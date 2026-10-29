package it.softsamurai.domain.model

/** A rectangle in topo units (the site's SVG viewBox). */
data class TopoRect(val x: Double, val y: Double, val width: Double, val height: Double)

/**
 * The topo of a sector, as drawn by falesiaonline.it: a photo of the wall placed in [frame] at [image], and the
 * line of each route over it. Every coordinate is in [frame] units.
 */
data class Topo(
    val settoreId: Long,
    val imageUrl: String?,
    val frame: TopoRect,
    val image: TopoRect,
    val lines: List<TopoLine>,
) {
    fun lineOf(viaId: Long): TopoLine? = lines.find { it.viaId == viaId }
}

/**
 * The line of one route: one SVG path (`M… c… s…`) per pitch, in order. A single-pitch route has one segment.
 */
data class TopoLine(val viaId: Long, val segments: List<String>)
