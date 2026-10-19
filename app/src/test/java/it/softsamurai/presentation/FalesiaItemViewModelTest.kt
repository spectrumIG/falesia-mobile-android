package it.softsamurai.presentation

import app.cash.turbine.test
import arrow.core.Either
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.usecase.GetAllFalesieUseCase
import it.softsamurai.domain.usecase.SyncCragsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
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
    private lateinit var syncCrags: SyncCragsUseCase
    private lateinit var viewModel: FalesiaItemViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllFalesie = mockk<GetAllFalesieUseCase>().also { every { it() } returns falesieFlow }
        syncCrags = mockk()
        viewModel = FalesiaItemViewModel(getAllFalesie, syncCrags)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
        val syncGate = CompletableDeferred<Unit>()
        coEvery { syncCrags() } coAnswers { syncGate.await(); Either.Right(Unit) }
        falesieFlow.emit(listOf(fakeFalesia("1")))

        viewModel.uiState.test {
            // The StateFlow conflates: Loading may already be replaced by Success when collection starts.
            if (awaitItem() is FalesiaItemUiState.Loading) awaitItem() // Success(isRefreshing=false)

            viewModel.onRefresh()

            val refreshing = awaitItem() as FalesiaItemUiState.Success
            assertTrue(refreshing.isRefreshing)

            syncGate.complete(Unit)

            val done = awaitItem() as FalesiaItemUiState.Success
            assertFalse(done.isRefreshing)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRefresh emits syncErrorEvent and resets isRefreshing on sync failure`() = runTest {
        coEvery { syncCrags() } returns Either.Left(SyncError.NetworkFailure(RuntimeException("timeout")))
        falesieFlow.emit(listOf(fakeFalesia("1")))
        // uiState is WhileSubscribed: keep a subscriber so it reflects the data.
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.syncErrorEvent.test {
            viewModel.onRefresh()
            awaitItem() // the Unit error event
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value as FalesiaItemUiState.Success
        assertFalse(state.isRefreshing)
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
