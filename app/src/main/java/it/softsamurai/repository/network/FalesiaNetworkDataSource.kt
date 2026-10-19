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
            parseElencoFalesie(document)
        }.mapLeft { SyncError.NetworkFailure(it) }

    override suspend fun fetchDettagliFalesia(url: String): Either<SyncError, FalesiaDetailNetworkModel> =
        Either.catch {
            val document: Document = Ksoup.parseGetRequest(url)
            parseDettagliFalesia(document)
        }.mapLeft { SyncError.NetworkFailure(it) }
}
