package it.softsamurai.domain.model

import arrow.optics.optics

typealias FalesiaGeoCoord = Pair<Double, Double>?

@optics
data class Falesia(
    val id: String,
    val nome: String,
    val zona: String,
    val pictureUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val numeroVie: Int,
    val linkDettagli: String,
    val vie: List<Via>,
    val lastUpdated: Long,
) {
    companion object
}
