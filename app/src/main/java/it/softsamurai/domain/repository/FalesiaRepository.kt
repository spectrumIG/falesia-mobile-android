package it.softsamurai.domain.repository

import arrow.core.Either
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Topo
import kotlinx.coroutines.flow.Flow

interface FalesiaRepository {

    // ── Observe (reactive, local DB) ─────────────────────────────────────
    fun observeAllFalesie(): Flow<List<Falesia>>
    fun observeFalesiaWithVie(falesiaId: String): Flow<Falesia?>

    /** The topo of a sector, downloaded with the crag's routes; null when the sector has none. */
    fun observeTopo(settoreId: Long): Flow<Topo?>

    // ── Read (one-shot, local DB) ─────────────────────────────────────────
    suspend fun getFalesiaById(falesiaId: String): Either<FalesiaError, Falesia>
    suspend fun countFalesie(): Int
    /** Returns the oldest lastUpdated timestamp across all crags, used for freshness checks. */
    suspend fun getOldestUpdateTimestamp(): Long?
    /** Returns the newest lastUpdated timestamp: when the last successful sync ran. */
    suspend fun getNewestUpdateTimestamp(): Long?

    // ── Write ─────────────────────────────────────────────────────────────
    suspend fun upsertFalesia(falesia: Falesia): Either<FalesiaError, Unit>
    suspend fun upsertFalesie(falesie: List<Falesia>): Either<FalesiaError, Unit>
    suspend fun deleteFalesia(falesiaId: String): Either<FalesiaError, Unit>
    suspend fun deleteAllFalesie(): Either<FalesiaError, Unit>

    // ── Sync (coordinates local + remote) ────────────────────────────────
    suspend fun syncFalesie(): Either<SyncError, Unit>

    /**
     * Downloads the routes and the topo of every sector of a crag, and its parking (on demand, see
     * `RefreshCragDetailsUseCase`).
     */
    suspend fun refreshCragDetails(falesiaId: String): Either<SyncError, Unit>
}
