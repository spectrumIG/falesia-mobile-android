package it.softsamurai.repository.model

typealias FalesiaGeoCoord = Pair<Double, Double>?

data class Falesia(
    val nome: String,
    val zona: String,
    val coordinate: FalesiaGeoCoord,
    val numeroVie: Int,
    val linkDettagli: String
)

data class FalesiaDettagli(
    val numeroVie: Int,
    val vie: List<Via>
)