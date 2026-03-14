package it.softsamurai.repository.network

import arrow.core.Either
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.fleeksoft.ksoup.network.parsePostRequest
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import it.softsamurai.domain.error.SyncError
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel
import javax.inject.Inject

interface FalesiaNetworkDataSource {
    suspend fun fetchElencoFalesie(): Either<SyncError, List<FalesiaListItemNetworkModel>>
    suspend fun fetchDettagliFalesia(url: String): Either<SyncError, FalesiaDetailNetworkModel>
}

private const val BASE_REFERRER = "https://www.falesiaonline.it/falesie/sud-europa.html"
private const val POST_BODY = "location_id=1&order=byinterest&shownnumberofcrags=110"

class FalesiaNetDataSourceImpl @Inject constructor(
    private val baseUrl: String,
) : FalesiaNetworkDataSource {

    override suspend fun fetchElencoFalesie(): Either<SyncError, List<FalesiaListItemNetworkModel>> =
        Either.catch {
            val document: Document = Ksoup.parsePostRequest(
                url = baseUrl,
                httpRequestBuilder = {
                    headers { append(HttpHeaders.Referrer, BASE_REFERRER) }
                    setBody(POST_BODY)
                },
            )
            document.select("li.leafletshadow").map { element ->
                val nome = element.selectFirst("h4 a")?.text() ?: "N/A"
                val routeCount =
                    element
                        .selectFirst("div.indicatorrow.firstrow span.indicator[title=Vie]")
                        ?.text()
                        ?.trim()
                        ?.toIntOrNull() ?: 0
                val relativeLink = element.selectFirst("h4 a")?.attr("href") ?: ""
                val detailUrl = "https://www.falesiaonline.it$relativeLink"

                // TODO: verify the correct CSS selector for the list-page thumbnail once the
                //       site HTML is inspected. Currently trying the first <img> in the card.
                val pictureUrl = element.selectFirst("img")?.attr("abs:src")?.takeIf { it.isNotBlank() }

                FalesiaListItemNetworkModel(
                    nome = nome,
                    pictureUrl = pictureUrl,
                    routeCount = routeCount,
                    detailUrl = detailUrl,
                )
            }
        }.mapLeft { SyncError.NetworkFailure(it) }

    override suspend fun fetchDettagliFalesia(url: String): Either<SyncError, FalesiaDetailNetworkModel> =
        Either.catch {
            val document: Document = Ksoup.parseGetRequest(url)

            val vie = document.select("div.route-details").map { element ->
                val grado = element.selectFirst("div.route-grade")?.text() ?: ""
                val nome = element.selectFirst("span.nome-via")?.text() ?: ""
                ViaNetworkModel(nome = nome, grado = grado)
            }

            // TODO: verify the correct CSS selectors for the crag photo once HTML is inspected.
            val pictureUrl = document.selectFirst(
                "img.crag-photo, .falesia-photo img, .crag-header img, .crag-image img, article > img",
            )?.attr("abs:src")?.takeIf { it.isNotBlank() }

            val (latitude, longitude) = document.extractCoordinates() ?: (null to null)

            FalesiaDetailNetworkModel(
                pictureUrl = pictureUrl,
                latitude = latitude,
                longitude = longitude,
                vie = vie,
            )
        }.mapLeft { SyncError.NetworkFailure(it) }

    /**
     * Attempts to extract geographic coordinates from Leaflet.js initialization patterns.
     *
     * Strategies (tried in order):
     * 1. `data-lat` / `data-lng` attributes on any element.
     * 2. `setView([lat, lng], zoom)` script pattern.
     * 3. `L.marker([lat, lng])` script pattern.
     */
    private fun Document.extractCoordinates(): Pair<Double, Double>? {
        // Strategy 1: data attributes
        selectFirst("[data-lat][data-lng]")?.let { el ->
            val lat = el.attr("data-lat").toDoubleOrNull()
            val lng = el.attr("data-lng").toDoubleOrNull()
            if (lat != null && lng != null) return lat to lng
        }

        val setViewPattern = Regex("""setView\(\s*\[\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*]""")
        val markerPattern = Regex("""L\.marker\(\s*\[\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*]""")

        for (script in select("script")) {
            val data = script.data()

            // Strategy 2: setView
            setViewPattern.find(data)?.let { match ->
                val lat = match.groupValues[1].toDoubleOrNull()
                val lng = match.groupValues[2].toDoubleOrNull()
                if (lat != null && lng != null) return lat to lng
            }

            // Strategy 3: L.marker
            markerPattern.find(data)?.let { match ->
                val lat = match.groupValues[1].toDoubleOrNull()
                val lng = match.groupValues[2].toDoubleOrNull()
                if (lat != null && lng != null) return lat to lng
            }
        }

        return null
    }
}
