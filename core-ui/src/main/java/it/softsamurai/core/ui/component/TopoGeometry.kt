package it.softsamurai.core.ui.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

// Geometry of a topo, kept apart from drawing: what can be pure is pure (and tested on the JVM), the paths are
// parsed and sampled once per topo, never per frame.

/** Distance between two samples of a line, in topo units: fine enough for a finger, cheap to scan. */
private const val SAMPLE_STEP = 4f

/** A topo to draw: the photo's place in [frame] and the route lines, all in [frame] units (the site's viewBox). */
@Immutable
data class TopoSpec(
    val frame: Rect,
    val image: Rect,
    val lines: List<TopoLineSpec>,
)

/** One route on the topo: its SVG path data, one per pitch, and the [label] drawn at its base (its number). */
@Immutable
data class TopoLineSpec(
    val id: Long,
    val segments: List<String>,
    val label: String,
)

/** Uniform scale then offset: topo units → canvas pixels, the topo fitted and centred in the canvas. */
@Immutable
data class TopoTransform(val scale: Float, val offset: Offset) {
    fun toCanvas(point: Offset): Offset = point * scale + offset
    fun toTopo(point: Offset): Offset = (point - offset) / scale
    fun toCanvas(rect: Rect): Rect = Rect(toCanvas(rect.topLeft), toCanvas(rect.bottomRight))
}

/** Fits [frame] in [size] keeping its aspect ratio, centred (letterbox), as SVG's `xMidYMid meet`. */
fun topoTransform(frame: Rect, size: Size): TopoTransform {
    if (frame.width <= 0f || frame.height <= 0f || size.isEmpty()) return TopoTransform(
        1f,
        Offset.Zero
    )
    val scale = min(size.width / frame.width, size.height / frame.height)
    val offset = Offset(
        x = (size.width - frame.width * scale) / 2f - frame.left * scale,
        y = (size.height - frame.height * scale) / 2f - frame.top * scale,
    )
    return TopoTransform(scale, offset)
}

/**
 * Index of the line closest to [point] among [lines] (each a list of polylines `x0, y0, x1, y1…`), or null when
 * none is within [maxDistance]. Every unit is the same (topo units).
 */
fun nearestLine(lines: List<List<FloatArray>>, point: Offset, maxDistance: Float): Int? {
    var best: Int? = null
    var bestDistance = maxDistance
    lines.forEachIndexed { index, polylines ->
        polylines.forEach { polyline ->
            val distance = polyline.distanceTo(point)
            if (distance <= bestDistance) {
                best = index
                bestDistance = distance
            }
        }
    }
    return best
}

/**
 * Keeps a zoomed content inside its box: with the content scaled by [scale] around the centre of [size], the
 * translation can move it at most by the part that overflows on each side.
 */
fun clampOffset(offset: Offset, scale: Float, size: Size): Offset {
    val maxX = max(0f, size.width * (scale - 1f) / 2f)
    val maxY = max(0f, size.height * (scale - 1f) / 2f)
    return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
}

/** Distance from [point] to the polyline `x0, y0, x1, y1…`; a single point counts as a polyline. */
internal fun FloatArray.distanceTo(point: Offset): Float {
    if (size < 2) return Float.MAX_VALUE
    var best = hypot(this[0] - point.x, this[1] - point.y)
    var i = 2
    while (i + 1 < size) {
        best = min(best, segmentDistance(this[i - 2], this[i - 1], this[i], this[i + 1], point))
        i += 2
    }
    return best
}

private fun segmentDistance(x1: Float, y1: Float, x2: Float, y2: Float, point: Offset): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    val lengthSquared = dx * dx + dy * dy
    val t = if (lengthSquared == 0f) 0f else
        (((point.x - x1) * dx + (point.y - y1) * dy) / lengthSquared).coerceIn(0f, 1f)
    return hypot(x1 + t * dx - point.x, y1 + t * dy - point.y)
}

/** A route line ready to draw and hit: the path, its samples and where its number and end dots go. */
@Immutable
internal class TopoLineGeometry(
    val id: Long,
    val label: String,
    /** All pitches, in topo units. */
    val path: Path,
    /** One polyline per pitch, for hit testing. */
    val samples: List<FloatArray>,
    /** Lowest end of the line (largest y): its base, where its number goes. */
    val base: Offset,
    /** The other end of the line: where it tops out. */
    val top: Offset,
)

/** A topo parsed and sampled once: [TopoView] keeps it while the topo does not change. */
@Immutable
internal class TopoGeometry(val frame: Rect, val image: Rect, val lines: List<TopoLineGeometry>) {
    val samples: List<List<FloatArray>> = lines.map { it.samples }
}

/** Parses every SVG path of [spec] (invalid ones are skipped) and samples it. */
internal fun TopoGeometry(spec: TopoSpec): TopoGeometry {
    val parser = PathParser()
    val measure = PathMeasure()
    val lines = spec.lines.mapNotNull { line ->
        val path = Path()
        val samples = mutableListOf<FloatArray>()
        val ends = mutableListOf<Offset>()
        line.segments.forEach { data ->
            parser.clear()
            val segment =
                runCatching { parser.parsePathString(data).toPath() }.getOrNull() ?: return@forEach
            measure.setPath(segment, false)
            val length = measure.length
            if (length <= 0f) return@forEach
            path.addPath(segment)
            samples += measure.sample(length)
            ends += measure.getPosition(0f)
            ends += measure.getPosition(length)
        }
        if (ends.isEmpty()) return@mapNotNull null
        val base = ends.maxBy { it.y }
        TopoLineGeometry(
            id = line.id,
            label = line.label,
            path = path,
            samples = samples,
            base = base,
            top = ends.minBy { it.y },
        )
    }
    return TopoGeometry(spec.frame, spec.image, lines)
}

private fun PathMeasure.sample(length: Float): FloatArray {
    val count = (length / SAMPLE_STEP).toInt().coerceAtLeast(1)
    val points = FloatArray((count + 1) * 2)
    for (i in 0..count) {
        val position = getPosition(length * i / count)
        points[i * 2] = position.x
        points[i * 2 + 1] = position.y
    }
    return points
}
