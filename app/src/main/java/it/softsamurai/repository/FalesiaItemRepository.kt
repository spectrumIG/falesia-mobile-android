package it.softsamurai.repository

import it.softsamurai.repository.database.FalesiaItem
import it.softsamurai.repository.database.FalesiaItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface DataSource

interface FalesiaItemRepository {
    val falesiaItems: Flow<List<String>>

    suspend fun addFalesiaToRepo(name: String)
}









//TODO - This won't be used for now.
class FalesiaDatabesLocalDataSource @Inject constructor(
    private val falesiaItemDao: FalesiaItemDao
) : FalesiaItemRepository {

    override val falesiaItems: Flow<List<String>> =
        falesiaItemDao.getFalesiaItems().map { items -> items.map { it.name } }

    override suspend fun addFalesiaToRepo(name: String) {
        falesiaItemDao.insertFalesiaItem(FalesiaItem(name = name))
    }
}
