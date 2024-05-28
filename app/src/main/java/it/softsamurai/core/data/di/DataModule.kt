
package it.softsamurai.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.softsamurai.core.data.DefaultFalesiaItemRepository
import it.softsamurai.core.data.FalesiaItemRepository
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
        falesiaItemRepository: DefaultFalesiaItemRepository
    ): FalesiaItemRepository
}

class FakeFalesiaItemRepository @Inject constructor() : FalesiaItemRepository {
    override val falesiaItems: Flow<List<String>> = flowOf(fakeFalesiaItems)

    override suspend fun add(name: String) {
        throw NotImplementedError()
    }
}

val fakeFalesiaItems = listOf("One", "Two", "Three")
