package it.softsamurai.repository.network

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.Method
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h4
import it.skrape.selects.html5.li
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

    private val baseUrl = "https://www.falesiaonline.it/it/cragsearch/orderCragListUpdate"

    override suspend fun getElencoFalesie(): List<Falesia> {
        return withContext(IO) {

            skrape(BrowserFetcher) {
                request {
                    headers =
                        mapOf("Referer" to "https://www.falesiaonline.it/falesie/sud-europa.html")
                    this.body = "location_id=1&order=byinterest&shownnumberofcrags=110"
                    url = baseUrl
                    method = Method.POST
                }

                response {
                    htmlDocument {
                        val falesie = mutableListOf<Falesia>()

                        li(".leafletshadow") {
                            findAll {
                                forEach { element ->
                                    // Estrazione del nome della falesia
                                    val nome = element.h4 {
                                        findFirst {
                                            a { findFirst { text } }
                                        }
                                    }

                                    // Estrazione del numero di vie
                                    div("indicatorrow firstrow") {

                                    }
                                    val numeroVie = div {
                                        span(".indicator[title=Vie]") {
                                            findFirst {
                                                text.trim().toIntOrNull() ?: 0
                                            }
                                        }

                                    }

                                    // Estrazione del link ai dettagli
                                    val linkDettagli = element.h4 {
                                        a { findFirst { attribute("href") } }
                                    }

                                    falesie.add(
                                        Falesia(
                                            nome = nome,
                                            numeroVie = numeroVie,

                                            linkDettagli = "https://www.falesiaonline.it$linkDettagli",
                                            zona = "",
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