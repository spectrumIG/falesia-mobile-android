package it.softsamurai.repository.local

import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.FalesiaSyncEntity
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import kotlinx.coroutines.flow.Flow

interface FalesiaLocalDataSource {

    fun observeAllFalesieWithVie(): Flow<List<FalesiaWithVie>>
    fun observeFalesiaWithVie(falesiaId: String): Flow<FalesiaWithVie?>

    suspend fun getFalesiaWithVieById(falesiaId: String): FalesiaWithVie?
    suspend fun countFalesie(): Int
    suspend fun getOldestUpdateTimestamp(): Long?
    suspend fun getNewestUpdateTimestamp(): Long?

    /**
     * Atomically upserts [entity] and replaces all its sectors and routes with [settori] and [vie].
     */
    suspend fun upsertFalesiaWithVie(
        entity: FalesiaEntity,
        settori: List<SettoreEntity>,
        vie: List<ViaEntity>
    )

    /**
     * Weekly sync of one crag, atomically: upserts its synced columns and [settori], and deletes the sectors
     * that are gone. The routes of the remaining sectors, the parking and `vieUpdatedAt` are kept.
     */
    suspend fun upsertFalesiaFromSync(entity: FalesiaSyncEntity, settori: List<SettoreEntity>)

    /**
     * On-demand details of one crag, atomically: replaces its routes with [vie] and stores the parking and
     * [updatedAt].
     */
    suspend fun replaceDettagli(
        falesiaId: String,
        vie: List<ViaEntity>,
        parking: Pair<Double, Double>?,
        updatedAt: Long,
    )

    suspend fun deleteFalesiaById(falesiaId: String)
    suspend fun deleteAllFalesie()
}
