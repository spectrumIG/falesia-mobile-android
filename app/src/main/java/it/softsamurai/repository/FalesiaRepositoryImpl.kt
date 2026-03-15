package it.softsamurai.repository

import arrow.core.Either
import arrow.core.raise.either
import it.softsamurai.core.di.IoDispatcher
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.repository.FalesiaRepository
import it.softsamurai.repository.local.FalesiaLocalDataSource
import it.softsamurai.repository.mapper.idFromUrl
import it.softsamurai.repository.mapper.toDomain
import it.softsamurai.repository.mapper.toEntity
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val SYNC_CONCURRENCY = 5

class FalesiaRepositoryImpl @Inject constructor(
    private val localDataSource: FalesiaLocalDataSource,
    private val networkDataSource: FalesiaNetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FalesiaRepository {

    // ── Observe ───────────────────────────────────────────────────────────

    override fun observeAllFalesie(): Flow<List<Falesia>> =
        localDataSource.observeAllFalesieWithVie().map { list -> list.map { it.toDomain() } }

    override fun observeFalesiaWithVie(falesiaId: String): Flow<Falesia?> =
        localDataSource.observeFalesiaWithVie(falesiaId).map { it?.toDomain() }

    // ── Read ──────────────────────────────────────────────────────────────

    override suspend fun getFalesiaById(falesiaId: String): Either<FalesiaError, Falesia> =
        either {
            localDataSource.getFalesiaWithVieById(falesiaId)?.toDomain()
                ?: raise(FalesiaError.NotFound(falesiaId))
        }

    override suspend fun countFalesie(): Int = localDataSource.countFalesie()

    override suspend fun getOldestUpdateTimestamp(): Long? =
        localDataSource.getOldestUpdateTimestamp()

    // ── Write ─────────────────────────────────────────────────────────────

    override suspend fun upsertFalesia(falesia: Falesia): Either<FalesiaError, Unit> =
        Either.catch {
            localDataSource.upsertFalesiaWithVie(
                entity = falesia.toEntity(),
                vie = falesia.vie.map { it.toEntity(falesia.id) },
            )
        }.mapLeft { FalesiaError.DatabaseError(it) }

    override suspend fun upsertFalesie(falesie: List<Falesia>): Either<FalesiaError, Unit> =
        Either.catch {
            falesie.forEach { falesia ->
                localDataSource.upsertFalesiaWithVie(
                    entity = falesia.toEntity(),
                    vie = falesia.vie.map { it.toEntity(falesia.id) },
                )
            }
        }.mapLeft { FalesiaError.DatabaseError(it) }

    override suspend fun deleteFalesia(falesiaId: String): Either<FalesiaError, Unit> =
        Either.catch { localDataSource.deleteFalesiaById(falesiaId) }
            .mapLeft { FalesiaError.DatabaseError(it) }

    override suspend fun deleteAllFalesie(): Either<FalesiaError, Unit> =
        Either.catch { localDataSource.deleteAllFalesie() }
            .mapLeft { FalesiaError.DatabaseError(it) }

    // ── Sync ──────────────────────────────────────────────────────────────

    override suspend fun syncFalesie(): Either<SyncError, Unit> = either {
        withContext(ioDispatcher) {
            // Step 1: fetch the crag list (single POST request)
            val listItems: List<FalesiaListItemNetworkModel> =
                networkDataSource.fetchElencoFalesie().bind()

            val now = System.currentTimeMillis()
            val semaphore = Semaphore(SYNC_CONCURRENCY)

            // Step 2: fetch each crag's detail page concurrently, bounded by semaphore
            val detailResults: List<Either<SyncError, Pair<FalesiaListItemNetworkModel, FalesiaDetailNetworkModel>>> =
                coroutineScope {
                    listItems.map { listItem ->
                        async {
                            semaphore.withPermit {
                                networkDataSource.fetchDettagliFalesia(listItem.detailUrl)
                                    .map { detail -> listItem to detail }
                            }
                        }
                    }.map { it.await() }
                }

            // Step 3: collect results — first failure short-circuits via bind()
            val pairs = detailResults.map { it.bind() }

            // Step 4: persist atomically per crag
            Either.catch {
                pairs.forEach { (listItem, detail) ->
                    val falesiaId = listItem.idFromUrl()
                    localDataSource.upsertFalesiaWithVie(
                        entity = listItem.toEntity(detail, now),
                        vie = detail.vie.map { v -> v.toEntity(falesiaId) },
                    )
                }
            }.mapLeft { SyncError.StorageFailure(it) }.bind()
        }
    }
}
