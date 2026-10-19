package it.softsamurai.repository.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.softsamurai.core.di.IoDispatcher
import it.softsamurai.domain.repository.FalesiaRepository
import it.softsamurai.domain.time.TimeProvider
import it.softsamurai.repository.FalesiaRepositoryImpl
import it.softsamurai.repository.local.FalesiaLocalDataSource
import it.softsamurai.repository.local.FalesiaLocalDataSourceImpl
import it.softsamurai.repository.network.FalesiaNetDataSourceImpl
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

private const val FALESIA_LIST_URL = "https://www.falesiaonline.it/falesie/sud-europa.html"

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFalesiaRepository(impl: FalesiaRepositoryImpl): FalesiaRepository

    @Binds
    @Singleton
    abstract fun bindFalesiaLocalDataSource(impl: FalesiaLocalDataSourceImpl): FalesiaLocalDataSource

    @Binds
    @Singleton
    abstract fun bindFalesiaNetworkDataSource(impl: FalesiaNetDataSourceImpl): FalesiaNetworkDataSource

    @Module
    @InstallIn(SingletonComponent::class)
    object DataProvides {

        @Provides
        @Singleton
        fun provideBaseUrl(): String = FALESIA_LIST_URL

        @Provides
        @Singleton
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @Singleton
        fun provideTimeProvider(): TimeProvider = TimeProvider { System.currentTimeMillis() }
    }
}
