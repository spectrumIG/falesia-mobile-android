package it.softsamurai.domain.model

import arrow.optics.optics

@optics
data class Via(
    /** falesiaonline.it route id. */
    val id: Long = 0L,
    val nome: String,
    /** French grade as written by the site (`6a+`); see [FrenchGrade] to compare grades. */
    val grado: String,
    val settoreId: Long = 0L,
    /** Average of the users' votes, 0–5 in half stars; null without votes. */
    val rating: Double? = null,
    val ratingCount: Int = 0,
    /** Route length as written by the site, e.g. `15–20m`. */
    val length: String = "",
    val style: ViaStyle = ViaStyle.Unknown,
) {
    companion object
}
