package it.softsamurai.presentation

import app.cash.turbine.test
import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.GeoPoint
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.sync.DownloadMode
import it.softsamurai.domain.usecase.GetDownloadModeUseCase
import it.softsamurai.domain.usecase.GetFalesiaWithVieUseCase
import it.softsamurai.domain.usecase.RefreshCragDetailsUseCase
import it.softsamurai.domain.usecase.RefreshOutcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CragDetailsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val crag = MutableStateFlow<Falesia?>(falesia())
    private val getFalesia =
        mockk<GetFalesiaWithVieUseCase>().also { every { it("arco") } returns crag }
    private val refreshDetails = mockk<RefreshCragDetailsUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { refreshDetails("arco", any()) } returns Either.Right(RefreshOutcome.Synced)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(mode: DownloadMode = DownloadMode.Automatic) = CragDetailsViewModel(
        "arco",
        getFalesia,
        refreshDetails,
        mockk<GetDownloadModeUseCase>().also { every { it() } returns mode },
    )

    private fun TestScope.observe(viewModel: CragDetailsViewModel) {
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }
    }

    private fun CragDetailsViewModel.content() = uiState.value as CragDetailsUiState.Content

    // ── reduce ──

    @Test
    fun `missing crag is NotFound`() {
        assertEquals(CragDetailsUiState.NotFound, reduceCragDetails(null, RoutesStatus.Idle, false))
    }

    @Test
    fun `content has the featured routes, the location and the navigation target`() {
        val state = reduceCragDetails(
            falesia(
                vie = listOf(via(1, rating = 3.0), via(2, rating = 5.0)),
                parking = GeoPoint(1.0, 2.0)
            ),
            RoutesStatus.Idle,
            isRefreshing = false,
        ) as CragDetailsUiState.Content

        assertEquals(listOf(2L, 1L), state.featured.map { it.id })
        assertEquals(GeoPoint(43.0, 10.0), state.location)
        assertEquals(GeoPoint(1.0, 2.0), state.navigationTarget)
    }

    @Test
    fun `loading and error show only when there are no saved routes`() {
        val empty = falesia()
        val withRoutes = falesia(vie = listOf(via(1)))

        assertTrue(
            (reduceCragDetails(
                empty,
                RoutesStatus.Loading,
                false
            ) as CragDetailsUiState.Content).routesLoading
        )
        assertTrue(
            (reduceCragDetails(
                empty,
                RoutesStatus.Failed,
                false
            ) as CragDetailsUiState.Content).routesError
        )
        assertFalse(
            (reduceCragDetails(
                withRoutes,
                RoutesStatus.Loading,
                false
            ) as CragDetailsUiState.Content).routesLoading
        )
        assertFalse(
            (reduceCragDetails(
                withRoutes,
                RoutesStatus.Failed,
                false
            ) as CragDetailsUiState.Content).routesError
        )
    }

    // ── On open ──

    @Test
    fun `opening the crag downloads its details once, not forced`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        coVerify(exactly = 1) { refreshDetails("arco", false) }
        assertFalse(viewModel.content().routesError)
    }

    @Test
    fun `a failed download without saved routes shows the error, Retry downloads again`() =
        runTest {
            coEvery { refreshDetails("arco", false) } returns Either.Left(
                SyncError.NetworkFailure(
                    RuntimeException()
                )
            )
            val viewModel = viewModel()
            observe(viewModel)

            assertTrue(viewModel.content().routesError)

            coEvery { refreshDetails("arco", false) } returns Either.Right(RefreshOutcome.Synced)
            viewModel.onIntent(CragDetailsIntent.Retry)

            assertFalse(viewModel.content().routesError)
            coVerify(exactly = 2) { refreshDetails("arco", false) }
        }

    @Test
    fun `routes saved by the download reach the screen through the db`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        crag.value = falesia(vie = listOf(via(7, rating = 4.0)))

        assertEquals(listOf(7L), viewModel.content().featured.map { it.id })
    }

    // ── Intents ──

    @Test
    fun `pull-to-refresh forces the download and reports an up-to-date result`() = runTest {
        coEvery {
            refreshDetails(
                "arco",
                true
            )
        } returns Either.Right(RefreshOutcome.AlreadyUpToDate)
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.effects.test {
            viewModel.onIntent(CragDetailsIntent.Refresh)
            assertEquals(CragDetailsEffect.AlreadyUpToDate, awaitItem())
        }
        assertFalse(viewModel.content().isRefreshing)
    }

    @Test
    fun `pull-to-refresh failure shows a snackbar and keeps the data`() = runTest {
        coEvery { refreshDetails("arco", true) } returns Either.Left(
            SyncError.NetworkFailure(
                RuntimeException()
            )
        )
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.effects.test {
            viewModel.onIntent(CragDetailsIntent.Refresh)
            assertEquals(CragDetailsEffect.SyncFailed, awaitItem())
        }
        assertEquals("Arco", viewModel.content().crag.nome)
    }

    @Test
    fun `pull-to-refresh is ignored while one is running`() = runTest {
        val pending = CompletableDeferred<Either<SyncError, RefreshOutcome>>()
        coEvery { refreshDetails("arco", true) } coAnswers { pending.await() }
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onIntent(CragDetailsIntent.Refresh)
        assertTrue(viewModel.content().isRefreshing)
        viewModel.onIntent(CragDetailsIntent.Refresh)
        pending.complete(Either.Right(RefreshOutcome.Synced))

        coVerify(exactly = 1) { refreshDetails("arco", true) }
        assertFalse(viewModel.content().isRefreshing)
    }

    @Test
    fun `open navigation goes to the parking with the crag name`() = runTest {
        crag.value = falesia(parking = GeoPoint(44.0, 9.0))
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.effects.test {
            viewModel.onIntent(CragDetailsIntent.OpenNavigation)
            assertEquals(CragDetailsEffect.OpenMaps(GeoPoint(44.0, 9.0), "Arco"), awaitItem())
        }
    }

    @Test
    fun `open navigation without any coordinates does nothing`() = runTest {
        crag.value = falesia().copy(latitude = null, longitude = null)
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.effects.test {
            viewModel.onIntent(CragDetailsIntent.OpenNavigation)
            expectNoEvents()
        }
    }

    @Test
    fun `see all opens the route list of this crag`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.effects.test {
            viewModel.onIntent(CragDetailsIntent.SeeAllRoutes)
            assertEquals(CragDetailsEffect.NavigateToRouteList("arco"), awaitItem())
        }
    }

    // ── Manual downloads (debug) ──

    @Test
    fun `manual mode flags routes never downloaded`() = runTest {
        crag.value = falesia().copy(vieUpdatedAt = null)
        val viewModel = viewModel(DownloadMode.Manual)
        observe(viewModel)

        assertTrue(viewModel.manualDownloads)
        assertTrue(viewModel.content().routesNotDownloaded)
    }

    @Test
    fun `routes downloaded once are not flagged, automatic mode never flags`() {
        val downloaded = reduceCragDetails(falesia().copy(vieUpdatedAt = 0L), RoutesStatus.Idle, false, true)
        val automatic = reduceCragDetails(falesia().copy(vieUpdatedAt = null), RoutesStatus.Idle, false, false)

        assertFalse((downloaded as CragDetailsUiState.Content).routesNotDownloaded)
        assertFalse((automatic as CragDetailsUiState.Content).routesNotDownloaded)
    }

    // ── Helpers ──

    private fun via(id: Long, rating: Double? = null) =
        Via(id = id, nome = "Via $id", grado = "6a", rating = rating, ratingCount = 1)

    private fun falesia(vie: List<Via> = emptyList(), parking: GeoPoint? = null) = Falesia(
        id = "arco",
        nome = "Arco",
        zona = "Trentino",
        pictureUrl = null,
        latitude = 43.0,
        longitude = 10.0,
        numeroVie = 10,
        linkDettagli = "",
        vie = vie,
        lastUpdated = 0L,
        parking = parking,
    )
}
