package it.softsamurai.domain.usecase

import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.repository.FalesiaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RefreshCragsUseCaseTest {

    private val now = 1_700_000_000_000L
    private lateinit var repository: FalesiaRepository
    private lateinit var useCase: RefreshCragsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RefreshCragsUseCase(repository) { now }
        coEvery { repository.syncFalesie() } returns Either.Right(Unit)
    }

    private fun givenDb(count: Int, lastSync: Long?) {
        coEvery { repository.countFalesie() } returns count
        coEvery { repository.getNewestUpdateTimestamp() } returns lastSync
    }

    // ── Skips the network ──

    @Test
    fun `recent sync keeps the db data and does not hit the network`() = runTest {
        givenDb(count = 90, lastSync = now - 60 * 60 * 1000L)

        val result = useCase()

        assertEquals(Either.Right(RefreshOutcome.AlreadyUpToDate), result)
        coVerify(exactly = 0) { repository.syncFalesie() }
    }

    @Test
    fun `sync done just under 24 hours ago is still recent`() = runTest {
        givenDb(count = 90, lastSync = now - MANUAL_REFRESH_INTERVAL_MILLIS + 1)

        assertEquals(Either.Right(RefreshOutcome.AlreadyUpToDate), useCase())
        coVerify(exactly = 0) { repository.syncFalesie() }
    }

    // ── Syncs ──

    @Test
    fun `sync done exactly 24 hours ago triggers a new sync`() = runTest {
        givenDb(count = 90, lastSync = now - MANUAL_REFRESH_INTERVAL_MILLIS)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase())
        coVerify(exactly = 1) { repository.syncFalesie() }
    }

    @Test
    fun `old data triggers a sync`() = runTest {
        givenDb(count = 90, lastSync = now - 3 * MANUAL_REFRESH_INTERVAL_MILLIS)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase())
        coVerify(exactly = 1) { repository.syncFalesie() }
    }

    @Test
    fun `empty db syncs even with a recent timestamp`() = runTest {
        givenDb(count = 0, lastSync = now)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase())
        coVerify(exactly = 1) { repository.syncFalesie() }
    }

    @Test
    fun `missing timestamp triggers a sync`() = runTest {
        givenDb(count = 90, lastSync = null)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase())
    }

    @Test
    fun `timestamp in the future (clock moved back) does not block refreshing`() = runTest {
        givenDb(count = 90, lastSync = now + 60_000L)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase())
        coVerify(exactly = 1) { repository.syncFalesie() }
    }

    @Test
    fun `sync failure is returned as Left`() = runTest {
        val error = SyncError.NetworkFailure(RuntimeException("offline"))
        givenDb(count = 90, lastSync = null)
        coEvery { repository.syncFalesie() } returns Either.Left(error)

        assertEquals(Either.Left(error), useCase())
    }
}
