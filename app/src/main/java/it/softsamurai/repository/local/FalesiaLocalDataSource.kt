package it.softsamurai.repository.local

import it.softsamurai.repository.database.entity.FalesiaEntity
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
     * Atomically upserts [entity] and replaces all its routes with [vie].
     * Existing routes for the crag are deleted before inserting the new ones.
     */
    suspend fun upsertFalesiaWithVie(entity: FalesiaEntity, vie: List<ViaEntity>)

    suspend fun deleteFalesiaById(falesiaId: String)
    suspend fun deleteAllFalesie()
}
