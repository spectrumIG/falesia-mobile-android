package it.softsamurai.core.testing

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import it.softsamurai.domain.sync.SyncMonitor
import it.softsamurai.domain.sync.SyncStatus
import it.softsamurai.sync.di.SyncMonitorModule
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

/** The test application does not initialise WorkManager: the startup sync is reported as done. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SyncMonitorModule::class])
object FakeSyncMonitorModule {

    @Provides
    @Singleton
    fun provideSyncMonitor(): SyncMonitor = SyncMonitor { flowOf(SyncStatus.Idle) }
}
