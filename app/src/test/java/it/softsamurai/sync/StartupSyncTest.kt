package it.softsamurai.sync

import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import it.softsamurai.domain.sync.DownloadMode
import it.softsamurai.repository.fixture.FixtureDataLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartupSyncTest {

    private val scheduler = mockk<SyncScheduler>(relaxed = true)
    private val loader = mockk<FixtureDataLoader>().also { coEvery { it.loadIfEmpty() } returns Either.Right(Unit) }

    @Test
    fun `automatic mode schedules the immediate and the weekly sync, no sample data`() = runTest {
        StartupSync({ DownloadMode.Automatic }, scheduler, loader).start(this)

        verify { scheduler.scheduleOnDemandSync() }
        verify { scheduler.schedulePeriodicSync() }
        coVerify(exactly = 0) { loader.loadIfEmpty() }
    }

    @Test
    fun `manual mode cancels every sync and loads the sample data`() = runTest(UnconfinedTestDispatcher()) {
        StartupSync({ DownloadMode.Manual }, scheduler, loader).start(this)

        verify { scheduler.cancelScheduledSyncs() }
        verify(exactly = 0) { scheduler.scheduleOnDemandSync() }
        verify(exactly = 0) { scheduler.schedulePeriodicSync() }
        coVerify { loader.loadIfEmpty() }
    }
}
