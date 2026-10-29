package it.softsamurai.repository.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import it.softsamurai.repository.database.entity.TopoEntity
import it.softsamurai.repository.database.entity.TopoLineEntity
import it.softsamurai.repository.database.relation.TopoWithLines
import kotlinx.coroutines.flow.Flow

@Dao
interface TopoDao {

    @Transaction
    @Query("SELECT * FROM topi WHERE settoreId = :settoreId")
    fun observeTopo(settoreId: Long): Flow<TopoWithLines?>

    @Upsert
    suspend fun upsertTopi(topi: List<TopoEntity>)

    @Upsert
    suspend fun upsertLines(lines: List<TopoLineEntity>)

    /** Deletes the topi of a crag and, by cascade, their lines. */
    @Query("DELETE FROM topi WHERE falesiaId = :falesiaId")
    suspend fun deleteTopiByFalesiaId(falesiaId: String)
}
