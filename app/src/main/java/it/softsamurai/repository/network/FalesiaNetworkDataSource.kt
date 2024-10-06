package it.softsamurai.repository.network

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.span
import it.softsamurai.repository.model.Falesia
import it.softsamurai.repository.model.FalesiaDettagli
import it.softsamurai.repository.model.Via

interface FalesiaNetworkDataSource {
    suspend fun getElencoFalesie(): List<Falesia>
    suspend fun getDettagliFalesia(url: String): FalesiaDettagli
}


class FalesiaRepositoryImpl : FalesiaNetworkDataSource {

    private val baseUrl = "https://www.falesiaonline.it/elenco-falesie"

    override suspend fun getElencoFalesie(): List<Falesia> {
        return skrape(HttpFetcher) {
            request {
                url = baseUrl
            }

            response {
                htmlDocument {
                    val falesie = mutableListOf<Falesia>()

                    // Seleziona la lista di falesie (corretto secondo la struttura HTML e skrape API aggiornata)
                    div(".falesia-list-item") {
                        findAll {
                            forEach { element ->
                                val nome = element.div(".falesia-nome").text
                                val zona = element.div(".falesia-zona").text
                                val coordinate = element.div(".falesia-coordinate").text
                                val numeroVie = element.div(".falesia-vie").text.toIntOrNull() ?: 0
                                val link = element.a().attribute("href") ?: ""

                                falesie.add(
                                    Falesia(
                                        nome = nome,
                                        zona = zona,
                                        coordinate = coordinate,
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

    override suspend fun getDettagliFalesia(falesiaUrl: String): FalesiaDettagli {
        return skrape(HttpFetcher) {
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

                    div(".via-details") {
                        findAll {
                            forEach { element ->
                                val grado = element.div(".via-grado").text
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
