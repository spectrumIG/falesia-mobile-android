package it.softsamurai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import it.softsamurai.sync.StartupSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@HiltAndroidApp
class FalesiaMobile : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var startupSync: StartupSync

    /** Work that outlives a screen, such as loading the debug sample data. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Tells WorkManager to use the Hilt-aware worker factory (required for @HiltWorker). */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Release: immediate + weekly sync. Debug: sample data only, downloads by hand (StartupSync).
        startupSync.start(appScope)
    }
}
