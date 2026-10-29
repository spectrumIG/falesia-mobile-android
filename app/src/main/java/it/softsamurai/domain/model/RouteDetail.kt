package it.softsamurai.domain.model

/**
 * One route of a crag with what its detail screen shows: its sector, the sector's routes in the site's order
 * (the routes drawn on the same topo) and the sector's topo, null when the site has none.
 */
data class RouteDetail(
    val crag: Falesia,
    val via: Via,
    val settore: Settore?,
    val sectorVie: List<Via>,
    val topo: Topo?,
)

/** The routes of a sector in the site's order (`Falesia.vie` is sorted by sector, then by route). */
fun Falesia.vieOfSettore(settoreId: Long): List<Via> = vie.filter { it.settoreId == settoreId }

/** Route id → its number on the sector's topo, from 1, in the order of the list (the site's order). */
fun List<Via>.routeNumbers(): Map<Long, Int> =
    withIndex().associate { (index, via) -> via.id to index + 1 }
