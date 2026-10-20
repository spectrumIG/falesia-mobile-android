package it.softsamurai.domain.usecase

import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.repository.FalesiaRepository
import it.softsamurai.domain.sync.DownloadMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RefreshCragDetailsUseCaseTest {

    private val now = 1_700_000_000_000L
    private lateinit var repository: FalesiaRepository
    private lateinit var useCase: RefreshCragDetailsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RefreshCragDetailsUseCase(repository, { now }, { DownloadMode.Automatic })
        coEvery { repository.refreshCragDetails("arco") } returns Either.Right(Unit)
    }

    private fun givenCrag(vieUpdatedAt: Long?) {
        coEvery { repository.getFalesiaById("arco") } returns Either.Right(
            Falesia(
                id = "arco",
                nome = "Arco",
                zona = "",
                pictureUrl = null,
                latitude = null,
                longitude = null,
                numeroVie = 0,
                linkDettagli = "",
                vie = emptyList(),
                lastUpdated = 0L,
                vieUpdatedAt = vieUpdatedAt,
            ),
        )
    }

    // ── Opening the crag ──

    @Test
    fun `routes never downloaded are downloaded`() = runTest {
        givenCrag(vieUpdatedAt = null)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase("arco"))
        coVerify(exactly = 1) { repository.refreshCragDetails("arco") }
    }

    @Test
    fun `routes downloaded just under a week ago are kept`() = runTest {
        givenCrag(vieUpdatedAt = now - ONE_WEEK_MILLIS + 1)

        assertEquals(Either.Right(RefreshOutcome.AlreadyUpToDate), useCase("arco"))
        coVerify(exactly = 0) { repository.refreshCragDetails(any()) }
    }

    @Test
    fun `routes downloaded exactly a week ago are downloaded again`() = runTest {
        givenCrag(vieUpdatedAt = now - ONE_WEEK_MILLIS)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase("arco"))
    }

    // ── Pull-to-refresh ──

    @Test
    fun `forced refresh within 24 hours does not hit the network`() = runTest {
        givenCrag(vieUpdatedAt = now - MANUAL_REFRESH_INTERVAL_MILLIS + 1)

        assertEquals(Either.Right(RefreshOutcome.AlreadyUpToDate), useCase("arco", force = true))
        coVerify(exactly = 0) { repository.refreshCragDetails(any()) }
    }

    @Test
    fun `forced refresh after 24 hours downloads again`() = runTest {
        givenCrag(vieUpdatedAt = now - MANUAL_REFRESH_INTERVAL_MILLIS)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase("arco", force = true))
    }

    @Test
    fun `a download in the future (clock moved back) does not block refreshing`() = runTest {
        givenCrag(vieUpdatedAt = now + 60_000L)

        assertEquals(Either.Right(RefreshOutcome.Synced), useCase("arco"))
    }

    // ── Manual downloads (debug) ──

    @Test
    fun `in manual mode opening the crag never downloads`() = runTest {
        val manual = RefreshCragDetailsUseCase(repository, { now }, { DownloadMode.Manual })
        givenCrag(vieUpdatedAt = null)

        assertEquals(Either.Right(RefreshOutcome.AlreadyUpToDate), manual("arco"))
        coVerify(exactly = 0) { repository.refreshCragDetails(any()) }
    }

    @Test
    fun `in manual mode the download button downloads stale routes`() = runTest {
        val manual = RefreshCragDetailsUseCase(repository, { now }, { DownloadMode.Manual })
        givenCrag(vieUpdatedAt = 0L)

        assertEquals(Either.Right(RefreshOutcome.Synced), manual("arco", force = true))
    }

    // ── Failures ──

    @Test
    fun `download failure is returned as Left`() = runTest {
        givenCrag(vieUpdatedAt = null)
        val error = SyncError.NetworkFailure(RuntimeException("offline"))
        coEvery { repository.refreshCragDetails("arco") } returns Either.Left(error)

        assertEquals(Either.Left(error), useCase("arco"))
    }

    @Test
    fun `unknown crag is Left CragNotFound without network`() = runTest {
        coEvery { repository.getFalesiaById("x") } returns Either.Left(FalesiaError.NotFound("x"))

        assertEquals(Either.Left(SyncError.CragNotFound("x")), useCase("x"))
        coVerify(exactly = 0) { repository.refreshCragDetails(any()) }
    }
}
