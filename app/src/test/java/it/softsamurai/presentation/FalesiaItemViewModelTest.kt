package it.softsamurai.presentation

import app.cash.turbine.test
import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.usecase.GetAllFalesieUseCase
import it.softsamurai.domain.usecase.RefreshCragsUseCase
import it.softsamurai.domain.usecase.RefreshOutcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private lateinit var viewModel: FalesiaItemViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllFalesie = mockk<GetAllFalesieUseCase>().also { every { it() } returns falesieFlow }
        refreshCrags = mockk()
        viewModel = FalesiaItemViewModel(getAllFalesie, refreshCrags)
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
}
