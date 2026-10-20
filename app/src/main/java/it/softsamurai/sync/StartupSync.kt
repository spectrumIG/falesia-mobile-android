package it.softsamurai.sync

import it.softsamurai.domain.sync.DownloadMode
import it.softsamurai.domain.sync.DownloadModeProvider
import it.softsamurai.repository.fixture.FixtureDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * What the app does with falesiaonline.it when it starts:
 * - [DownloadMode.Automatic] (release): immediate sync (KEEP: no-op if already enqueued) and the weekly one;
 * - [DownloadMode.Manual] (debug): no sync at all (scheduled ones are cancelled) and the saved sample pages
 *   fill an empty DB. Only the download buttons hit the site.
 */
class StartupSync @Inject constructor(
    private val downloadMode: DownloadModeProvider,
    private val syncScheduler: SyncScheduler,
    private val fixtureDataLoader: FixtureDataLoader,
) {
    fun start(scope: CoroutineScope) {
        when (downloadMode.current()) {
            DownloadMode.Automatic -> {
                syncScheduler.scheduleOnDemandSync()
                syncScheduler.schedulePeriodicSync()
            }
            DownloadMode.Manual -> {
                syncScheduler.cancelScheduledSyncs()
                scope.launch { fixtureDataLoader.loadIfEmpty() }
            }
        }
    }
}
