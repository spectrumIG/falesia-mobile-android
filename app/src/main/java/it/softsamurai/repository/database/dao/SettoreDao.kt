package it.softsamurai.repository.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.softsamurai.repository.database.entity.SettoreEntity

@Dao
interface SettoreDao {

    @Query("SELECT * FROM settori WHERE falesiaId = :falesiaId ORDER BY position")
    suspend fun getSettoriByFalesiaId(falesiaId: String): List<SettoreEntity>

    /** Upsert updates rows in place, so the routes of an existing sector (cascade) are kept. */
    @Upsert
    suspend fun upsertSettori(settori: List<SettoreEntity>)

    /** Removes the sectors no longer on the crag page, with their routes (cascade). */
    @Query("DELETE FROM settori WHERE falesiaId = :falesiaId AND id NOT IN (:keepIds)")
    suspend fun deleteSettoriNotIn(falesiaId: String, keepIds: List<Long>)

    @Query("DELETE FROM settori WHERE falesiaId = :falesiaId")
    suspend fun deleteSettoriByFalesiaId(falesiaId: String)
}
