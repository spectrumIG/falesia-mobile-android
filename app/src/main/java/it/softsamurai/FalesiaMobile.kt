package it.softsamurai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import it.softsamurai.sync.SyncScheduler
import javax.inject.Inject

@HiltAndroidApp
class FalesiaMobile : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncScheduler: SyncScheduler

    /** Tells WorkManager to use the Hilt-aware worker factory (required for @HiltWorker). */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Trigger an immediate sync (KEEP policy — no-op if already enqueued).
        syncScheduler.scheduleOnDemandSync()
        // Register the weekly background sync.
        syncScheduler.schedulePeriodicSync()
    }
}
