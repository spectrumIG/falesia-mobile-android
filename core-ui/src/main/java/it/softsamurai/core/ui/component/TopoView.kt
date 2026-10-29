package it.softsamurai.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import kotlin.math.max
import kotlin.math.roundToInt

private val LineWidth = 2.5.dp
private val SelectedLineWidth = 4.dp

/** Extra width of the dark halo under each line: keeps it readable on light rock (the design's drop shadow). */
private val HaloWidth = 2.dp
private val EndDotRadius = 4.dp
private val BadgeMinRadius = 9.dp
private val BadgePadding = 3.dp

/** How far from a line a tap still selects it. */
private val TouchRadius = 24.dp
private const val LINE_ALPHA = 0.85f
private const val HALO_ALPHA = 0.45f
private const val MAX_ZOOM = 5f

/** The route line [TopoView] highlights, for UI tests. */
val TopoSelectedLine = SemanticsPropertyKey<Long?>("TopoSelectedLine")
var SemanticsPropertyReceiver.topoSelectedLine by TopoSelectedLine

/**
 * A sector topo (design component `TopoView`): the wall photo with every route line drawn over it, numbered at
 * its base; the selected line in `primary`, with dots at both ends, the others in white. Keeps the topo's aspect
 * ratio: the site's photos are mostly landscape, a 3:4 crop would cut their lines.
 *
 * Built for many lines and frequent selection changes:
 * - the SVG paths are parsed and sampled once per [spec] ([TopoGeometry]);
 * - strokes, transform and label layouts are cached per size (`drawWithCache`), paths are drawn in topo units
 *   through the canvas transform, so they are never copied;
 * - [selectedId] is read only while drawing: a new selection redraws, without recomposition or layout.
 *
 * [image] receives the modifier that places the photo where the topo puts it (`AsyncImage` in the app).
 * [onLineClick] gets the id of the line nearest to a tap, within [TouchRadius].
 */
@Composable
fun TopoView(
    spec: TopoSpec,
    selectedId: () -> Long?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onLineClick: ((Long) -> Unit)? = null,
    image: @Composable (Modifier) -> Unit = {},
) {
    val geometry = remember(spec) { TopoGeometry(spec) }
    val textMeasurer = rememberTextMeasurer()
    val colors = TopoColors(
        selected = MaterialTheme.colorScheme.primary,
        onSelected = MaterialTheme.colorScheme.onPrimary,
        line = Color.White.copy(alpha = LINE_ALPHA),
        halo = Color.Black.copy(alpha = HALO_ALPHA),
        badge = FalesiaTheme.colors.overlayStrong,
        onBadge = FalesiaTheme.colors.inkOnPhoto,
    )
    val labelStyle = FalesiaTheme.typography.micro
    val currentOnLineClick by rememberUpdatedState(onLineClick)
    val frame = geometry.frame
    Box(
        modifier = modifier
            .aspectRatio(if (frame.height > 0f) frame.width / frame.height else 1f)
            .clipToBounds()
            .background(FalesiaTheme.colors.primary10)
            .semantics {
                this.contentDescription = contentDescription
                topoSelectedLine = selectedId()
            }
            .then(
                if (onLineClick == null) {
                    Modifier
                } else {
                    Modifier.pointerInput(geometry) {
                        detectTapGestures { tap ->
                            val transform = topoTransform(frame, size.toSize())
                            nearestLine(
                                lines = geometry.samples,
                                point = transform.toTopo(tap),
                                maxDistance = TouchRadius.toPx() / transform.scale,
                            )?.let { index -> currentOnLineClick?.invoke(geometry.lines[index].id) }
                        }
                    }
                },
            )
            .drawWithCache {
                val transform = topoTransform(frame, size)

                // Paths are in topo units and drawn through the canvas scale: divide widths by it.
                fun stroke(width: Float) = Stroke(
                    width = width / transform.scale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )

                val lineStroke = stroke(LineWidth.toPx())
                val haloStroke = stroke((LineWidth + HaloWidth).toPx())
                val selectedStroke = stroke(SelectedLineWidth.toPx())
                val selectedHaloStroke = stroke((SelectedLineWidth + HaloWidth).toPx())
                val badges = geometry.lines.map { line ->
                    val layout = textMeasurer.measure(line.label, labelStyle)
                    TopoBadgeLayout(
                        id = line.id,
                        text = layout,
                        center = transform.toCanvas(line.base),
                        radius = max(
                            BadgeMinRadius.toPx(),
                            layout.size.width / 2f + BadgePadding.toPx()
                        ),
                    )
                }
                val endDotRadius = EndDotRadius.toPx()
                onDrawWithContent {
                    drawContent()
                    val selected = selectedId()
                    val selectedLine = geometry.lines.find { it.id == selected }
                    translate(transform.offset.x, transform.offset.y) {
                        scale(transform.scale, transform.scale, pivot = Offset.Zero) {
                            geometry.lines.forEach { line ->
                                if (line !== selectedLine) {
                                    drawPath(line.path, colors.halo, style = haloStroke)
                                    drawPath(line.path, colors.line, style = lineStroke)
                                }
                            }
                            selectedLine?.let { line ->
                                drawPath(line.path, colors.halo, style = selectedHaloStroke)
                                drawPath(line.path, colors.selected, style = selectedStroke)
                            }
                        }
                    }
                    selectedLine?.let { line ->
                        drawCircle(colors.selected, endDotRadius, transform.toCanvas(line.top))
                    }
                    badges.forEach { badge ->
                        val isSelected = badge.id == selected
                        drawCircle(
                            if (isSelected) colors.selected else colors.badge,
                            badge.radius,
                            badge.center
                        )
                        drawText(
                            textLayoutResult = badge.text,
                            color = if (isSelected) colors.onSelected else colors.onBadge,
                            topLeft = badge.center - Offset(
                                badge.text.size.width / 2f,
                                badge.text.size.height / 2f
                            ),
                        )
                    }
                }
            },
    ) {
        image(Modifier.topoImage(geometry))
    }
}

/**
 * The topo badge (`fm-topo-badge`): the grade in a `primary` pill and a [label], on the `overlay` backdrop. Sits
 * over the photo, bottom left.
 */
@Composable
fun TopoBadge(grade: String, label: String, modifier: Modifier = Modifier) {
    val colors = FalesiaTheme.colors
    val spacing = FalesiaTheme.spacing
    Row(
        modifier = modifier
            .background(colors.overlay, RoundedCornerShape(FalesiaTheme.radius.lg))
            .border(
                BorderStroke(FalesiaTheme.size.hairline, colors.overlayLine),
                RoundedCornerShape(FalesiaTheme.radius.lg)
            )
            .padding(spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        Text(
            text = grade,
            style = FalesiaTheme.typography.itemTitle.copy(fontSize = FalesiaTheme.typography.bodySm.fontSize),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(FalesiaTheme.radius.default)
                )
                .padding(horizontal = spacing.space2, vertical = BadgeVerticalPadding),
        )
        Text(text = label, style = FalesiaTheme.typography.chip, color = colors.inkOnPhoto)
    }
}

private val BadgeVerticalPadding = 2.dp

/** Pinch-zoom and pan state of [TopoZoomBox], saved across configuration changes. */
@Stable
class TopoZoomState(scale: Float = 1f, offset: Offset = Offset.Zero) {
    var scale by mutableFloatStateOf(scale)
        private set
    var offset by mutableStateOf(offset)
        private set
    internal var contentSize = Size.Zero

    /** Applies a gesture: [zoom] multiplies the scale (1x–5x), [pan] moves the content, kept in bounds. */
    fun transform(zoom: Float, pan: Offset) {
        val newScale = (scale * zoom).coerceIn(1f, MAX_ZOOM)
        offset = clampOffset(offset * (newScale / scale) + pan, newScale, contentSize)
        scale = newScale
    }

    companion object {
        val Saver = listSaver<TopoZoomState, Float>(
            save = { listOf(it.scale, it.offset.x, it.offset.y) },
            restore = { TopoZoomState(it[0], Offset(it[1], it[2])) },
        )
    }
}

@Composable
fun rememberTopoZoomState(): TopoZoomState =
    rememberSaveable(saver = TopoZoomState.Saver) { TopoZoomState() }

/**
 * Pinch-zoom and pan around [content] (the full-screen topo). The transform is applied in the graphics layer and
 * read only there: a gesture neither recomposes nor redraws the topo, the layer is reused. Taps still reach the
 * content, in its own coordinates.
 */
@Composable
fun TopoZoomBox(
    modifier: Modifier = Modifier,
    state: TopoZoomState = rememberTopoZoomState(),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(state) {
                detectTransformGestures { _, pan, zoom, _ -> state.transform(zoom, pan) }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .onSizeChanged { state.contentSize = it.toSize() }
                .graphicsLayer {
                    scaleX = state.scale
                    scaleY = state.scale
                    translationX = state.offset.x
                    translationY = state.offset.y
                },
            content = content,
        )
    }
}

@Immutable
private data class TopoColors(
    val selected: Color,
    val onSelected: Color,
    val line: Color,
    val halo: Color,
    val badge: Color,
    val onBadge: Color,
)

private class TopoBadgeLayout(
    val id: Long,
    val text: TextLayoutResult,
    val center: Offset,
    val radius: Float,
)

/** Measures and places the photo on the rectangle the topo gives it, in the box's pixels. */
private fun Modifier.topoImage(geometry: TopoGeometry): Modifier =
    layout { measurable, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val rect: Rect = topoTransform(
            geometry.frame,
            Size(width.toFloat(), height.toFloat())
        ).toCanvas(geometry.image)
        val placeable = measurable.measure(
            Constraints.fixed(
                rect.width.roundToInt().coerceAtLeast(0),
                rect.height.roundToInt().coerceAtLeast(0)
            ),
        )
        layout(width, height) { placeable.place(rect.left.roundToInt(), rect.top.roundToInt()) }
    }

// ──────────────────────── Previews ────────────────────────

/** Three routes of Monsummano's "Parete principale" (falesiaonline.it), the middle one with two pitches. */
private val previewTopo = TopoSpec(
    frame = Rect(0f, 0f, 400f, 300f),
    image = Rect(0f, 0f, 400f, 300f),
    lines = listOf(
        TopoLineSpec(1, listOf("M60,60c0,0-6.5,30-12.5,48.5s-22,84.5-22,84.5s-10,60-10,90"), "1"),
        TopoLineSpec(
            2,
            listOf("M180,150c0,0-8,40-14,70s-12,70-12,70", "M200,40c0,0-4,30-8,60s-12,50-12,50"),
            "2",
        ),
        TopoLineSpec(
            3,
            listOf("M330,30c0,0-17,13-27,22.5c-6.9,6.5-13.8,14.5-17.4,22.8s-10,80-6,120s8,95,8,95"),
            "3"
        ),
    ),
)

@PreviewLightDark
@Composable
private fun TopoViewPreview() {
    AppTheme {
        Box(Modifier.background(FalesiaTheme.colors.bg)) {
            TopoView(
                spec = previewTopo,
                selectedId = { 2L },
                contentDescription = "Topo",
                modifier = Modifier.fillMaxWidth(),
            )
            TopoBadge(
                grade = "6a+",
                label = "Topo",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(FalesiaTheme.spacing.space4),
            )
        }
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun TopoViewLargeFontPreview() {
    AppTheme {
        Column {
            TopoView(
                spec = previewTopo,
                selectedId = { 1L },
                contentDescription = "Topo",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
