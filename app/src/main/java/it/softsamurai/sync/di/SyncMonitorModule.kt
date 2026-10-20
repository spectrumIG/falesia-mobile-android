package it.softsamurai.sync.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.softsamurai.domain.sync.SyncMonitor
import it.softsamurai.sync.WorkManagerSyncMonitor
import javax.inject.Singleton

/** Separate from [SyncModule] so instrumented tests can replace only the monitor (no WorkManager there). */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncMonitorModule {

    @Binds
    @Singleton
    abstract fun bindSyncMonitor(impl: WorkManagerSyncMonitor): SyncMonitor
}
