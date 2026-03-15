package it.softsamurai.repository.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.softsamurai.repository.database.entity.ViaEntity

@Dao
interface ViaDao {

    @Query("SELECT * FROM vie WHERE falesiaId = :falesiaId")
    suspend fun getVieByFalesiaId(falesiaId: String): List<ViaEntity>

    @Upsert
    suspend fun upsertVia(via: ViaEntity)

    @Upsert
    suspend fun upsertVie(vie: List<ViaEntity>)

    @Query("DELETE FROM vie WHERE falesiaId = :falesiaId")
    suspend fun deleteVieByFalesiaId(falesiaId: String)

    @Query("DELETE FROM vie WHERE id = :id")
    suspend fun deleteViaById(id: Long)

    @Query("DELETE FROM vie")
    suspend fun deleteAllVie()
}
