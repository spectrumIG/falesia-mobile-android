package it.softsamurai.repository.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.softsamurai.repository.DefaultFalesiaRepo
import it.softsamurai.repository.FalesiaItemRepository
import it.softsamurai.repository.network.FalesiaNetDataSourceImpl
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
//interface DataModule {
//
//
//    @Singleton
//    @Binds
//    fun bindsFalesiaItemRepository(
//        falesiaItemRepository: FalesiaNetworkDataSource
//    ): FalesiaItemRepository
//}

@Module
@InstallIn(SingletonComponent::class)
object FalesiaRepoModule{

    @Provides
    @Singleton
    fun provideFalesiaUrlForDataSource(): String =
        "https://www.falesiaonline.it/falesie/sud-europa.html"

    @Provides
    @Singleton
    fun provideFalesiaRepo(falesiaNetworkDataSource: FalesiaNetworkDataSource): FalesiaItemRepository = DefaultFalesiaRepo(falesiaNetworkDataSource)

    @Provides
    @Singleton
    fun provideFalesiaLocalDataSource(baseUrl: String): FalesiaNetworkDataSource = FalesiaNetDataSourceImpl(baseUrl= baseUrl)
}

//@Module
//@InstallIn(SingletonComponent::class)
//interface FalesiaDataSourceModule {
//
//}
