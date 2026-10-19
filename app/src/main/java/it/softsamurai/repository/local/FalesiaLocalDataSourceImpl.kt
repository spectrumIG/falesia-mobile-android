package it.softsamurai.repository.local

import androidx.room.withTransaction
import it.softsamurai.repository.database.AppDatabase
import it.softsamurai.repository.database.dao.FalesiaDao
import it.softsamurai.repository.database.dao.ViaDao
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FalesiaLocalDataSourceImpl @Inject constructor(
    private val db: AppDatabase,
    private val falesiaDao: FalesiaDao,
    private val viaDao: ViaDao,
) : FalesiaLocalDataSource {

    override fun observeAllFalesieWithVie(): Flow<List<FalesiaWithVie>> =
        falesiaDao.observeAllFalesieWithVie()

    override fun observeFalesiaWithVie(falesiaId: String): Flow<FalesiaWithVie?> =
        falesiaDao.observeFalesiaWithVie(falesiaId)

    override suspend fun getFalesiaWithVieById(falesiaId: String): FalesiaWithVie? =
        falesiaDao.getFalesiaWithVieById(falesiaId)

    override suspend fun countFalesie(): Int = falesiaDao.countFalesie()

    override suspend fun getOldestUpdateTimestamp(): Long? = falesiaDao.getOldestUpdateTimestamp()

    override suspend fun getNewestUpdateTimestamp(): Long? = falesiaDao.getNewestUpdateTimestamp()

    override suspend fun upsertFalesiaWithVie(entity: FalesiaEntity, vie: List<ViaEntity>) {
        db.withTransaction {
            falesiaDao.upsertFalesia(entity)
            viaDao.deleteVieByFalesiaId(entity.id)
            if (vie.isNotEmpty()) {
                viaDao.upsertVie(vie)
            }
        }
    }

    override suspend fun deleteFalesiaById(falesiaId: String) =
        falesiaDao.deleteFalesiaById(falesiaId)

    override suspend fun deleteAllFalesie() = falesiaDao.deleteAllFalesie()
}
