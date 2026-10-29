package it.softsamurai.repository.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.softsamurai.repository.database.AppDatabase
import it.softsamurai.repository.database.dao.FalesiaDao
import it.softsamurai.repository.database.dao.SettoreDao
import it.softsamurai.repository.database.dao.TopoDao
import it.softsamurai.repository.database.dao.ViaDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        // The DB is a cache of falesiaonline.it: a schema change drops it and the next sync downloads it again.
        Room.databaseBuilder(context, AppDatabase::class.java, "falesia_db")
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()

    @Provides
    fun provideFalesiaDao(db: AppDatabase): FalesiaDao = db.falesiaDao()

    @Provides
    fun provideSettoreDao(db: AppDatabase): SettoreDao = db.settoreDao()

    @Provides
    fun provideViaDao(db: AppDatabase): ViaDao = db.viaDao()

    @Provides
    fun provideTopoDao(db: AppDatabase): TopoDao = db.topoDao()
}
