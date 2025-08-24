package it.softsamurai.repository.network

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.fleeksoft.ksoup.network.parsePostRequest
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import it.softsamurai.presentation.FalesiaItemUiState
import it.softsamurai.repository.model.Falesia
import it.softsamurai.repository.model.FalesiaDettagli
import it.softsamurai.repository.model.Via
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface FalesiaNetworkDataSource {
    suspend fun getElencoFalesie(): Flow<List<Falesia>>
    suspend fun getDettagliFalesia(url: String): FalesiaDettagli
}

class FalesiaNetDataSourceImpl @Inject constructor(val baseUrl: String) : FalesiaNetworkDataSource {


    override suspend fun getElencoFalesie(): Flow<List<Falesia>> {
        val request: Document = Ksoup.parsePostRequest(
            url = baseUrl,
            httpRequestBuilder = {
                headers {
                    append(
                        HttpHeaders.Referrer,
                        "https://www.falesiaonline.it/falesie/sud-europa.html"
                    )
                }
                setBody("location_id=1&order=byinterest&shownnumberofcrags=110")
            })

      val falesie =   request.select("li.leafletshadow").let { elements ->
            val falesie = elements.map { element ->
                // Estrazione del nome della falesia
                val nome = element.selectFirst("h4 a")?.text() ?: "N/A"

                // Estrazione del numero di vie
                val numeroVie = element.selectFirst("div.indicatorrow.firstrow span.indicator[title=Vie]")?.text()
                    ?.trim()?.toIntOrNull() ?: 0

                // Estrazione del link ai dettagli
                val linkDettagli = element.selectFirst("h4 a")?.attr("href") ?: ""

                Falesia(
                    nome = nome,
                    numeroVie = numeroVie,
                    linkDettagli = "https://www.falesiaonline.it$linkDettagli",
                    zona = ""
                )
            }

            return@let falesie
        }


        return flowOf(falesie).catch {
            FalesiaItemUiState.Error(message = "ERRORE,Acciderbolina!!!")
        }
    }


    override suspend fun getDettagliFalesia(falesiaUrl: String): FalesiaDettagli {
        return withContext(IO) {
            val document: Document = Ksoup.parseGetRequest(falesiaUrl)

            var numeroVie = 0
            val vie = mutableListOf<Via>()

            // Trova il numero di vie
            document.selectFirst("div.numero-vie")?.text()?.let { text ->
                numeroVie = text.toIntOrNull() ?: 0
            }

            // Trova tutti gli elementi route-details
            document.select("div.route-details").forEach { element ->
                val grado = element.selectFirst("div.route-grade")?.text() ?: ""
                val nome = element.selectFirst("span.nome-via")?.text() ?: ""
                vie.add(Via(nome = nome, grado = grado))
            }

            FalesiaDettagli(numeroVie, vie)
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