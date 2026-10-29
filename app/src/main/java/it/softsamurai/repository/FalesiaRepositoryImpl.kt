package it.softsamurai.repository

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import it.softsamurai.core.di.IoDispatcher
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Topo
import it.softsamurai.domain.repository.FalesiaRepository
import it.softsamurai.domain.time.TimeProvider
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.local.FalesiaLocalDataSource
import it.softsamurai.repository.mapper.idFromUrl
import it.softsamurai.repository.mapper.lineEntities
import it.softsamurai.repository.mapper.toDomain
import it.softsamurai.repository.mapper.toEntity
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.SettorePageNetworkModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val SYNC_CONCURRENCY = 5

class FalesiaRepositoryImpl @Inject constructor(
    private val localDataSource: FalesiaLocalDataSource,
    private val networkDataSource: FalesiaNetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val timeProvider: TimeProvider,
) : FalesiaRepository {

    // ── Observe ───────────────────────────────────────────────────────────

    override fun observeAllFalesie(): Flow<List<Falesia>> =
        localDataSource.observeAllFalesieWithVie().map { list -> list.map { it.toDomain() } }

    override fun observeFalesiaWithVie(falesiaId: String): Flow<Falesia?> =
        localDataSource.observeFalesiaWithVie(falesiaId).map { it?.toDomain() }

    override fun observeTopo(settoreId: Long): Flow<Topo?> =
        localDataSource.observeTopo(settoreId).map { it?.toDomain() }

    // ── Read ──────────────────────────────────────────────────────────────

    override suspend fun getFalesiaById(falesiaId: String): Either<FalesiaError, Falesia> =
        either {
            localDataSource.getFalesiaWithVieById(falesiaId)?.toDomain()
                ?: raise(FalesiaError.NotFound(falesiaId))
        }

    override suspend fun countFalesie(): Int = localDataSource.countFalesie()

    override suspend fun getOldestUpdateTimestamp(): Long? =
        localDataSource.getOldestUpdateTimestamp()

    override suspend fun getNewestUpdateTimestamp(): Long? =
        localDataSource.getNewestUpdateTimestamp()

    // ── Write ─────────────────────────────────────────────────────────────

    override suspend fun upsertFalesia(falesia: Falesia): Either<FalesiaError, Unit> =
        Either.catch {
            falesia.save()
        }.mapLeft { FalesiaError.DatabaseError(it) }

    override suspend fun upsertFalesie(falesie: List<Falesia>): Either<FalesiaError, Unit> =
        Either.catch {
            falesie.forEach { it.save() }
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

            val now = timeProvider.nowMillis()

            // Step 2: fetch each crag's detail page concurrently, SYNC_CONCURRENCY at a time
            val detailResults: List<Either<SyncError, Pair<FalesiaListItemNetworkModel, FalesiaDetailNetworkModel>>> =
                buildList {
                    for (chunk in listItems.chunked(SYNC_CONCURRENCY)) {
                        addAll(
                            coroutineScope {
                                chunk.map { listItem ->
                                    async {
                                        networkDataSource.fetchDettagliFalesia(listItem.detailUrl)
                                            .map { detail -> listItem to detail }
                                    }
                                }.awaitAll()
                            }
                        )
                    }
                }

            // Step 3: collect results — first failure short-circuits via bind()
            val pairs = detailResults.map { it.bind() }

            // Step 4: persist atomically per crag. Routes and parking are on-demand (refreshCragDetails):
            // the sync keeps them for the sectors that are still there.
            Either.catch {
                pairs.forEach { (listItem, detail) ->
                    val falesiaId = listItem.idFromUrl()
                    localDataSource.upsertFalesiaFromSync(
                        entity = listItem.toEntity(detail, now),
                        settori = detail.settori.mapIndexed { index, settore ->
                            settore.toEntity(
                                falesiaId,
                                index
                            )
                        },
                    )
                }
            }.mapLeft { SyncError.StorageFailure(it) }.bind()
        }
    }

    override suspend fun refreshCragDetails(falesiaId: String): Either<SyncError, Unit> = either {
        withContext(ioDispatcher) {
            val saved = ensureNotNull(localDataSource.getFalesiaWithVieById(falesiaId)) {
                SyncError.CragNotFound(falesiaId)
            }

            // Step 1: fetch every sector page (routes and topo), SYNC_CONCURRENCY at a time; first failure
            // short-circuits
            val results: List<Either<SyncError, Pair<SettoreEntity, SettorePageNetworkModel>>> =
                buildList {
                    for (chunk in saved.settori.sortedBy { it.position }
                        .chunked(SYNC_CONCURRENCY)) {
                        addAll(
                            coroutineScope {
                                chunk.map { settore ->
                                    async {
                                        networkDataSource.fetchSettore(settore.linkDettagli)
                                            .map { page -> settore to page }
                                    }
                                }.awaitAll()
                            }
                        )
                    }
                }
            val pages = results.map { it.bind() }
            val vie = pages
                .flatMap { (settore, page) ->
                    page.vie.mapIndexed { index, via -> via.toEntity(falesiaId, settore.id, index) }
                }
                // A route listed in two sectors is stored once, in the first one.
                .distinctBy { it.id }
            val topi = pages.mapNotNull { (settore, page) -> page.topo?.let { settore.id to it } }

            // Step 2: the parking is optional: if the map page fails, keep the one already saved
            val parking = networkDataSource.fetchParcheggio(saved.falesia.linkDettagli).getOrElse {
                saved.falesia.parkingLatitude?.let { lat -> saved.falesia.parkingLongitude?.let { lng -> lat to lng } }
            }

            // Step 3: persist routes, topi, parking and timestamp atomically
            Either.catch {
                localDataSource.replaceDettagli(
                    falesiaId = falesiaId,
                    vie = vie,
                    topi = topi.map { (settoreId, topo) -> topo.toEntity(falesiaId, settoreId) },
                    lines = topi.flatMap { (settoreId, topo) -> topo.lineEntities(settoreId) },
                    parking = parking,
                    updatedAt = timeProvider.nowMillis(),
                )
            }.mapLeft { SyncError.StorageFailure(it) }.bind()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private suspend fun Falesia.save() {
        localDataSource.upsertFalesiaWithVie(
            entity = toEntity(),
            settori = settori.mapIndexed { index, settore -> settore.toEntity(id, index) },
            vie = vie.mapIndexed { index, via -> via.toEntity(id, index) },
        )
    }
}
