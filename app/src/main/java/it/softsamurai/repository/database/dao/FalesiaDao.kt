package it.softsamurai.repository.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import kotlinx.coroutines.flow.Flow

@Dao
interface FalesiaDao {

    // ── Observe ───────────────────────────────────────────────────────────

    @Transaction
    @Query("SELECT * FROM falesie ORDER BY nome ASC")
    fun observeAllFalesieWithVie(): Flow<List<FalesiaWithVie>>

    @Transaction
    @Query("SELECT * FROM falesie WHERE id = :id")
    fun observeFalesiaWithVie(id: String): Flow<FalesiaWithVie?>

    // ── Read ──────────────────────────────────────────────────────────────

    @Transaction
    @Query("SELECT * FROM falesie WHERE id = :id")
    suspend fun getFalesiaWithVieById(id: String): FalesiaWithVie?

    @Query("SELECT COUNT(*) FROM falesie")
    suspend fun countFalesie(): Int

    /** Returns the oldest lastUpdated timestamp (useful to detect stale data). */
    @Query("SELECT MIN(lastUpdated) FROM falesie")
    suspend fun getOldestUpdateTimestamp(): Long?

    // ── Write ─────────────────────────────────────────────────────────────

    @Upsert
    suspend fun upsertFalesia(falesia: FalesiaEntity)

    @Upsert
    suspend fun upsertFalesie(falesie: List<FalesiaEntity>)

    @Query("DELETE FROM falesie WHERE id = :id")
    suspend fun deleteFalesiaById(id: String)

    @Query("DELETE FROM falesie")
    suspend fun deleteAllFalesie()
}
