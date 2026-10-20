package it.softsamurai.repository.network

import arrow.core.Either
import arrow.core.flatMap
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

    /** Routes of one sector, from its page ([it.softsamurai.repository.network.model.SettoreNetworkModel.detailUrl]). */
    suspend fun fetchVieSettore(url: String): Either<SyncError, List<ViaNetworkModel>>

    /** Parking position (lat to lng) from the map page of the crag at [detailUrl]; null when it has none. */
    suspend fun fetchParcheggio(detailUrl: String): Either<SyncError, Pair<Double, Double>?>
}

private const val BASE_REFERRER = "https://www.falesiaonline.it/falesie/sud-europa.html"
private const val POST_BODY = "location_id=1&order=byinterest&shownnumberofcrags=110"

class FalesiaNetDataSourceImpl @Inject constructor(
    private val baseUrl: String,
) : FalesiaNetworkDataSource {

    override suspend fun fetchElencoFalesie(): Either<SyncError, List<FalesiaListItemNetworkModel>> =
        Either.catch {
            Ksoup.parsePostRequest(
                url = baseUrl,
                httpRequestBuilder = {
                    headers { append(HttpHeaders.Referrer, BASE_REFERRER) }
                    setBody(POST_BODY)
                },
            )
        }.mapLeft { SyncError.NetworkFailure(it) }.flatMap(::parseElencoFalesieOrFail)

    override suspend fun fetchDettagliFalesia(url: String): Either<SyncError, FalesiaDetailNetworkModel> =
        get(url).flatMap(::parseDettagliFalesia)

    override suspend fun fetchVieSettore(url: String): Either<SyncError, List<ViaNetworkModel>> =
        get(url).flatMap(::parseVieSettore)

    override suspend fun fetchParcheggio(detailUrl: String): Either<SyncError, Pair<Double, Double>?> =
        get(mapUrlFromDetailUrl(detailUrl)).map(::parseParcheggio)

    private suspend fun get(url: String): Either<SyncError, Document> =
        Either.catch { Ksoup.parseGetRequest(url) }.mapLeft { SyncError.NetworkFailure(it) }
}
