package it.softsamurai.domain.model

import arrow.optics.optics

@optics
data class Via(
    val id: Long = 0L,
    val nome: String,
    val grado: String,
) {
    companion object
}
