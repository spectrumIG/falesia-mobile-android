package it.softsamurai.repository.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.FalesiaSyncEntity
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

    /** Returns the newest lastUpdated timestamp: when the last successful sync ran. */
    @Query("SELECT MAX(lastUpdated) FROM falesie")
    suspend fun getNewestUpdateTimestamp(): Long?

    // ── Write ─────────────────────────────────────────────────────────────

    @Upsert
    suspend fun upsertFalesia(falesia: FalesiaEntity)

    @Upsert
    suspend fun upsertFalesie(falesie: List<FalesiaEntity>)

    /** Weekly sync: writes only the synced columns, keeping the on-demand details. */
    @Upsert(entity = FalesiaEntity::class)
    suspend fun upsertFalesiaSync(falesia: FalesiaSyncEntity)

    @Query(
        "UPDATE falesie SET parkingLatitude = :parkingLatitude, parkingLongitude = :parkingLongitude, " +
                "vieUpdatedAt = :vieUpdatedAt WHERE id = :id",
    )
    suspend fun updateDettagli(
        id: String,
        parkingLatitude: Double?,
        parkingLongitude: Double?,
        vieUpdatedAt: Long
    )

    @Query("DELETE FROM falesie WHERE id = :id")
    suspend fun deleteFalesiaById(id: String)

    @Query("DELETE FROM falesie")
    suspend fun deleteAllFalesie()
}
