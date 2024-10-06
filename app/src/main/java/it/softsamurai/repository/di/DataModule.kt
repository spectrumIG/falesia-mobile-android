
package it.softsamurai.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.softsamurai.repository.FalesiaDatabesLocalDataSource
import it.softsamurai.repository.FalesiaItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsFalesiaItemRepository(
        falesiaItemRepository: FalesiaDatabesLocalDataSource
    ): FalesiaItemRepository
}

class FakeFalesiaItemRepository @Inject constructor() : FalesiaItemRepository {
    override val falesiaItems: Flow<List<String>> = flowOf(fakeFalesiaItems)

    override suspend fun addFalesiaToRepo(name: String) {
      TODO()
    }
}

val fakeFalesiaItems = listOf("One", "Two", "Three")
