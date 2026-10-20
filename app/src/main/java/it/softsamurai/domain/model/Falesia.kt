package it.softsamurai.domain.model

import arrow.optics.optics

typealias FalesiaGeoCoord = Pair<Double, Double>?

@optics
data class Falesia(
    val id: String,
    val nome: String,
    /** Area and region, most specific first: `Toscana Nord Ovest, Toscana`. */
    val zona: String,
    val pictureUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val numeroVie: Int,
    val linkDettagli: String,
    val vie: List<Via>,
    val lastUpdated: Long,
    val description: String = "",
    /** Average of the users' votes, 0–5 in half stars; null without votes. */
    val rating: Double? = null,
    val ratingCount: Int = 0,
    /** Rock type as written by the site (`Calcare`). */
    val rock: String = "",
    /** Best seasons as written by the site (`Inverno, Mezze stagioni`). */
    val seasons: String = "",
    val parking: GeoPoint? = null,
    val settori: List<Settore> = emptyList(),
    /** Epoch millis of the last download of the sectors' routes and the parking; null if never downloaded. */
    val vieUpdatedAt: Long? = null,
) {
    companion object
}
