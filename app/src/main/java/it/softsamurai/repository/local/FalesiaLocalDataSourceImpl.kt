package it.softsamurai.repository.local

import androidx.room.withTransaction
import it.softsamurai.repository.database.AppDatabase
import it.softsamurai.repository.database.dao.FalesiaDao
import it.softsamurai.repository.database.dao.SettoreDao
import it.softsamurai.repository.database.dao.TopoDao
import it.softsamurai.repository.database.dao.ViaDao
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.FalesiaSyncEntity
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.database.entity.TopoEntity
import it.softsamurai.repository.database.entity.TopoLineEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import it.softsamurai.repository.database.relation.TopoWithLines
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FalesiaLocalDataSourceImpl @Inject constructor(
    private val db: AppDatabase,
    private val falesiaDao: FalesiaDao,
    private val settoreDao: SettoreDao,
    private val viaDao: ViaDao,
    private val topoDao: TopoDao,
) : FalesiaLocalDataSource {

    override fun observeAllFalesieWithVie(): Flow<List<FalesiaWithVie>> =
        falesiaDao.observeAllFalesieWithVie()

    override fun observeFalesiaWithVie(falesiaId: String): Flow<FalesiaWithVie?> =
        falesiaDao.observeFalesiaWithVie(falesiaId)

    override fun observeTopo(settoreId: Long): Flow<TopoWithLines?> = topoDao.observeTopo(settoreId)

    override suspend fun getFalesiaWithVieById(falesiaId: String): FalesiaWithVie? =
        falesiaDao.getFalesiaWithVieById(falesiaId)

    override suspend fun countFalesie(): Int = falesiaDao.countFalesie()

    override suspend fun getOldestUpdateTimestamp(): Long? = falesiaDao.getOldestUpdateTimestamp()

    override suspend fun getNewestUpdateTimestamp(): Long? = falesiaDao.getNewestUpdateTimestamp()

    override suspend fun upsertFalesiaWithVie(
        entity: FalesiaEntity,
        settori: List<SettoreEntity>,
        vie: List<ViaEntity>,
    ) {
        db.withTransaction {
            falesiaDao.upsertFalesia(entity)
            // Deleting the sectors deletes their routes too (cascade).
            settoreDao.deleteSettoriByFalesiaId(entity.id)
            viaDao.deleteVieByFalesiaId(entity.id)
            if (settori.isNotEmpty()) settoreDao.upsertSettori(settori)
            if (vie.isNotEmpty()) viaDao.upsertVie(vie)
        }
    }

    override suspend fun upsertFalesiaFromSync(
        entity: FalesiaSyncEntity,
        settori: List<SettoreEntity>
    ) {
        db.withTransaction {
            falesiaDao.upsertFalesiaSync(entity)
            settoreDao.deleteSettoriNotIn(entity.id, settori.map { it.id })
            if (settori.isNotEmpty()) settoreDao.upsertSettori(settori)
        }
    }

    override suspend fun replaceDettagli(
        falesiaId: String,
        vie: List<ViaEntity>,
        topi: List<TopoEntity>,
        lines: List<TopoLineEntity>,
        parking: Pair<Double, Double>?,
        updatedAt: Long,
    ) {
        db.withTransaction {
            viaDao.deleteVieByFalesiaId(falesiaId)
            if (vie.isNotEmpty()) viaDao.upsertVie(vie)
            // Deleting the topi deletes their lines too (cascade).
            topoDao.deleteTopiByFalesiaId(falesiaId)
            if (topi.isNotEmpty()) topoDao.upsertTopi(topi)
            if (lines.isNotEmpty()) topoDao.upsertLines(lines)
            falesiaDao.updateDettagli(falesiaId, parking?.first, parking?.second, updatedAt)
        }
    }

    override suspend fun deleteFalesiaById(falesiaId: String) =
        falesiaDao.deleteFalesiaById(falesiaId)

    override suspend fun deleteAllFalesie() = falesiaDao.deleteAllFalesie()
}
