package it.softsamurai.domain.model

import arrow.optics.optics

/** A point on the map. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/** How the routes are protected: bolted routes are sport routes. */
enum class ViaStyle { Sport, Trad, Mixed, Unknown }

/** Compass direction a wall faces. */
enum class Exposure { N, NE, E, SE, S, SW, W, NW }

/**
 * A sector (*settore*) of a crag: a wall with its own routes. The routes themselves are on the sector's page
 * and are loaded on demand, see `Falesia.vieUpdatedAt`.
 */
@optics
data class Settore(
    /** falesiaonline.it sector id (`/settorefoto/1510/…` → 1510). */
    val id: Long,
    val nome: String,
    val numeroVie: Int,
    /** Easiest and hardest grade as written by the site, blank when unknown. */
    val gradoMin: String,
    val gradoMax: String,
    /** Route length as written by the site, e.g. `13–28m`. */
    val length: String,
    /** Approach walk in minutes, from the parking. */
    val approachMinMinutes: Int?,
    val approachMaxMinutes: Int?,
    val exposures: List<Exposure>,
    val rating: Double?,
    val ratingCount: Int,
    val style: ViaStyle,
    val location: GeoPoint?,
    val linkDettagli: String,
) {
    companion object
}
