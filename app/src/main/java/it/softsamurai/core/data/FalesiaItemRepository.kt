package it.softsamurai.core.data

import it.softsamurai.reposiitory.database.FalesiaItem
import it.softsamurai.reposiitory.database.FalesiaItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface FalesiaItemRepository {
    val falesiaItems: Flow<List<String>>

    suspend fun add(name: String)
}

class DefaultFalesiaItemRepository @Inject constructor(
    private val falesiaItemDao: FalesiaItemDao
) : FalesiaItemRepository {

    override val falesiaItems: Flow<List<String>> =
        falesiaItemDao.getFalesiaItems().map { items -> items.map { it.name } }

    override suspend fun add(name: String) {
        falesiaItemDao.insertFalesiaItem(FalesiaItem(name = name))
    }
}
