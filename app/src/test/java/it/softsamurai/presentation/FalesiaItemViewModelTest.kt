package it.softsamurai.presentation

import app.cash.turbine.test
import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.sync.DownloadMode
import it.softsamurai.domain.sync.SyncStatus
import it.softsamurai.domain.usecase.GetDownloadModeUseCase
import it.softsamurai.domain.usecase.GetAllFalesieUseCase
import it.softsamurai.domain.usecase.ObserveSyncStatusUseCase
import it.softsamurai.domain.usecase.RefreshCragsUseCase
import it.softsamurai.domain.usecase.RefreshOutcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FalesiaItemViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val falesieFlow = MutableSharedFlow<List<Falesia>>(replay = 1)
    private lateinit var getAllFalesie: GetAllFalesieUseCase
    private lateinit var refreshCrags: RefreshCragsUseCase
    private val syncStatus = MutableStateFlow(SyncStatus.Running)
    private lateinit var viewModel: FalesiaItemViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllFalesie = mockk<GetAllFalesieUseCase>().also { every { it() } returns falesieFlow }
        refreshCrags = mockk()
        val observeSyncStatus =
            mockk<ObserveSyncStatusUseCase>().also { every { it() } returns syncStatus }
        viewModel = FalesiaItemViewModel(getAllFalesie, refreshCrags, observeSyncStatus, downloadMode(DownloadMode.Automatic))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** uiState is WhileSubscribed: keep a subscriber so it reflects the data. */
    private fun TestScope.observeUiState() {
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }
    }

    private fun successState() = viewModel.uiState.value as FalesiaItemUiState.Success

    @Test
    fun `uiState is Loading when db is empty`() = runTest {
        viewModel.uiState.test {
            assertEquals(FalesiaItemUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState becomes Success with data when db emits non-empty list`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial Loading
            falesieFlow.emit(listOf(fakeFalesia("1"), fakeFalesia("2")))
            val success = awaitItem() as FalesiaItemUiState.Success
            assertEquals(listOf(fakeFalesia("1"), fakeFalesia("2")), success.data)
            assertFalse(success.isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRefresh sets isRefreshing true then false in uiState`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { refreshCrags() } coAnswers { gate.await(); Either.Right(RefreshOutcome.Synced) }
        falesieFlow.emit(listOf(fakeFalesia("1")))

        viewModel.uiState.test {
            // The StateFlow conflates: Loading may already be replaced by Success when collection starts.
            if (awaitItem() is FalesiaItemUiState.Loading) awaitItem() // Success(isRefreshing=false)

            viewModel.onRefresh()

            val refreshing = awaitItem() as FalesiaItemUiState.Success
            assertTrue(refreshing.isRefreshing)

            gate.complete(Unit)

            val done = awaitItem() as FalesiaItemUiState.Success
            assertFalse(done.isRefreshing)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `recent data emits AlreadyUpToDate and keeps showing the db data`() = runTest {
        coEvery { refreshCrags() } returns Either.Right(RefreshOutcome.AlreadyUpToDate)
        val cached = listOf(fakeFalesia("1"), fakeFalesia("2"))
        falesieFlow.emit(cached)
        observeUiState()

        viewModel.events.test {
            viewModel.onRefresh()
            assertEquals(DiscoverEvent.AlreadyUpToDate, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(cached, successState().data)
        assertFalse(successState().isRefreshing)
    }

    @Test
    fun `successful sync emits no event`() = runTest {
        coEvery { refreshCrags() } returns Either.Right(RefreshOutcome.Synced)
        falesieFlow.emit(listOf(fakeFalesia("1")))

        viewModel.events.test {
            viewModel.onRefresh()
            expectNoEvents()
        }
    }

    @Test
    fun `onRefresh emits SyncFailed and resets isRefreshing on sync failure`() = runTest {
        coEvery { refreshCrags() } returns Either.Left(SyncError.NetworkFailure(RuntimeException("timeout")))
        falesieFlow.emit(listOf(fakeFalesia("1")))
        observeUiState()

        viewModel.events.test {
            viewModel.onRefresh()
            assertEquals(DiscoverEvent.SyncFailed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(successState().isRefreshing)
    }

    @Test
    fun `onRefresh while a refresh is running does not start another one`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { refreshCrags() } coAnswers { gate.await(); Either.Right(RefreshOutcome.Synced) }
        falesieFlow.emit(listOf(fakeFalesia("1")))

        viewModel.onRefresh()
        viewModel.onRefresh()
        viewModel.onRefresh()
        gate.complete(Unit)

        coVerify(exactly = 1) { refreshCrags() }
    }

    @Test
    fun `a new refresh is accepted once the previous one is done`() = runTest {
        coEvery { refreshCrags() } returns Either.Right(RefreshOutcome.AlreadyUpToDate)

        viewModel.onRefresh()
        viewModel.onRefresh()

        coVerify(exactly = 2) { refreshCrags() }
    }

    private fun fakeFalesia(id: String) = Falesia(
        id = id,
        nome = "Falesia $id",
        zona = "",
        pictureUrl = null,
        latitude = null,
        longitude = null,
        numeroVie = 10,
        linkDettagli = "https://example.com/$id",
        vie = emptyList(),
        lastUpdated = 0L,
    )

    private fun downloadMode(mode: DownloadMode) = mockk<GetDownloadModeUseCase>().also { every { it() } returns mode }

    @Test
    fun `manual downloads follow the download mode`() {
        assertFalse(viewModel.manualDownloads)
        val debug = FalesiaItemViewModel(
            getAllFalesie,
            refreshCrags,
            mockk<ObserveSyncStatusUseCase>().also { every { it() } returns syncStatus },
            downloadMode(DownloadMode.Manual),
        )
        assertTrue(debug.manualDownloads)
    }

    // ── Empty DB and failed sync ──

    @Test
    fun `empty db with a failed startup sync shows the error instead of loading`() = runTest {
        observeUiState()
        falesieFlow.emit(emptyList())

        syncStatus.value = SyncStatus.Failed

        assertEquals(FalesiaItemUiState.SyncFailed, viewModel.uiState.value)
    }

    @Test
    fun `empty db keeps loading while the sync is pending or running`() = runTest {
        observeUiState()
        falesieFlow.emit(emptyList())

        listOf(SyncStatus.Pending, SyncStatus.Running, SyncStatus.Idle).forEach {
            syncStatus.value = it
            assertEquals(FalesiaItemUiState.Loading, viewModel.uiState.value)
        }
    }

    @Test
    fun `saved crags are shown even when the sync failed`() = runTest {
        observeUiState()
        syncStatus.value = SyncStatus.Failed

        falesieFlow.emit(listOf(fakeFalesia("arco")))

        assertTrue(viewModel.uiState.value is FalesiaItemUiState.Success)
    }

    @Test
    fun `retry from the error loads, then shows the error again if it fails`() = runTest {
        val pending = CompletableDeferred<Either<SyncError, RefreshOutcome>>()
        coEvery { refreshCrags() } coAnswers { pending.await() }
        observeUiState()
        falesieFlow.emit(emptyList())
        syncStatus.value = SyncStatus.Failed

        viewModel.onRefresh()
        assertEquals(FalesiaItemUiState.Loading, viewModel.uiState.value)
        pending.complete(Either.Left(SyncError.NetworkFailure(RuntimeException("502"))))

        assertEquals(FalesiaItemUiState.SyncFailed, viewModel.uiState.value)
        coVerify(exactly = 1) { refreshCrags() }
    }

    @Test
    fun `a failed retry shows the error even if the background sync is still pending`() = runTest {
        coEvery { refreshCrags() } returns Either.Left(SyncError.NetworkFailure(RuntimeException("offline")))
        observeUiState()
        falesieFlow.emit(emptyList())
        syncStatus.value = SyncStatus.Pending

        viewModel.onRefresh()

        assertEquals(FalesiaItemUiState.SyncFailed, viewModel.uiState.value)
    }
}
