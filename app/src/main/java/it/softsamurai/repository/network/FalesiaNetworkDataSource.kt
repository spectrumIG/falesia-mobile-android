package it.softsamurai.repository.network

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.span
import it.softsamurai.repository.model.Falesia
import it.softsamurai.repository.model.FalesiaDettagli
import it.softsamurai.repository.model.Via
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface FalesiaNetworkDataSource {
    suspend fun getElencoFalesie(): List<Falesia>
    suspend fun getDettagliFalesia(url: String): FalesiaDettagli
}

class FalesiaNetDataSourceImpl @Inject constructor() : FalesiaNetworkDataSource {

    private val baseUrl = "https://www.falesiaonline.it/falesie/sud-europa.html"

    override suspend fun getElencoFalesie(): List<Falesia> {
        return withContext(IO) {

            skrape(BrowserFetcher) {
                request {
                    url = baseUrl
                }

                response {
                    htmlDocument {
                        val falesie = mutableListOf<Falesia>()

                        // Correzione dei selettori CSS per la lista delle falesie
                        div(".block-content") {
                            findAll {
                                forEach { element ->
                                    val nome = element.div(".falesia-item-name") {
                                        findFirst { text }
                                    }
                                    val zona = element.div(".falesia-item-area") {
                                        findFirst { text }
                                    }
                                    val coordinate = element.div(".falesia-item-coordinates") {
                                        findFirst { text }
                                    }
                                    val numeroVie = element.div(".falesia-item-routes") {
                                        findFirst { text.toIntOrNull() ?: 0 }
                                    }
                                    val link = element.a(".falesia-item-link") {
                                        findFirst { attribute("href") }
                                    }

                                    falesie.add(
                                        Falesia(
                                            nome = nome,
                                            zona = zona,
                                            coordinate = coordinate.toCoordinates(),
                                            numeroVie = numeroVie,
                                            linkDettagli = "https://www.falesiaonline.it$link"
                                        )
                                    )
                                }
                            }
                        }
                        falesie
                    }
                }
            }
        }
    }

    override suspend fun getDettagliFalesia(falesiaUrl: String): FalesiaDettagli {
        return withContext(IO) {
            skrape(BrowserFetcher) {
                request {
                    url = falesiaUrl
                }

                response {
                    htmlDocument {
                        var numeroVie = 0
                        val vie = mutableListOf<Via>()

                        div(".numero-vie") {
                            numeroVie = findFirst { text.toIntOrNull() ?: 0 }
                        }

                        div("route-details") {
                            findAll {
                                forEach { element ->
                                    val grado = element.div(".route-grade") {
                                        findFirst { text }
                                    }
                                    val nome = span(".nome-via") { findFirst { text } }
                                    vie.add(Via(nome = nome, grado = grado))
                                }
                            }
                        }
                        FalesiaDettagli(numeroVie, vie)
                    }
                }
            }
        }
    }
}

fun String.toCoordinates(): Pair<Double, Double>? {
    val coordinates = this.split(",").map { it.trim().toDoubleOrNull() }
    return if (coordinates.size == 2 && coordinates[0] != null && coordinates[1] != null) {
        Pair(coordinates[0]!!, coordinates[1]!!)
    } else {
        null
    }
}