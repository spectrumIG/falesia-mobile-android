package it.softsamurai.sync.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.softsamurai.sync.SyncScheduler
import it.softsamurai.sync.WorkManagerSyncScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler

    @Module
    @InstallIn(SingletonComponent::class)
    object SyncProvides {

        /**
         * WorkManager is initialised on-demand (see [FalesiaMobile.workManagerConfiguration]).
         * The first call to [WorkManager.getInstance] triggers initialisation using the
         * [HiltWorkerFactory] provided by the Application.
         */
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)
    }
}
