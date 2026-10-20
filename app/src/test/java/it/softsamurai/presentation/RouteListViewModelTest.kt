package it.softsamurai.presentation

import arrow.core.Either
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.GradeBand
import it.softsamurai.domain.model.Settore
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.domain.usecase.GetFalesiaWithVieUseCase
import it.softsamurai.domain.usecase.RefreshCragDetailsUseCase
import it.softsamurai.domain.usecase.RefreshOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class RouteListViewModelTest {

    private val crag = falesia()

    // ── filterVie ──

    @Test
    fun `without filters every route is listed, grouped by sector in order`() {
        val sections = filterVie(crag, RouteFilters())

        assertEquals(listOf(1L, 2L), sections.map { it.settore.id })
        assertEquals(listOf("Sole", "Luna"), sections.first().vie.map { it.nome })
    }

    @Test
    fun `query matches part of the name ignoring case`() {
        assertEquals(
            listOf("Luna"),
            filterVie(crag, RouteFilters(query = "un")).flatMap { s -> s.vie.map { it.nome } })
    }

    @Test
    fun `query matches the start of the grade`() {
        val names =
            filterVie(crag, RouteFilters(query = "6a")).flatMap { s -> s.vie.map { it.nome } }

        assertEquals(listOf("Sole", "Stella"), names)
    }

    @Test
    fun `grade band, sector and style filters combine`() {
        assertEquals(
            listOf("Luna"),
            filterVie(
                crag,
                RouteFilters(band = GradeBand.Seven)
            ).flatMap { s -> s.vie.map { it.nome } },
        )
        assertEquals(listOf(2L), filterVie(crag, RouteFilters(settoreId = 2)).map { it.settore.id })
        assertEquals(
            listOf("Stella"),
            filterVie(
                crag,
                RouteFilters(style = ViaStyle.Trad)
            ).flatMap { s -> s.vie.map { it.nome } },
        )
        assertTrue(
            filterVie(
                crag,
                RouteFilters(band = GradeBand.Seven, style = ViaStyle.Trad)
            ).isEmpty()
        )
    }

    @Test
    fun `routes with an unknown grade never match a grade band`() {
        val withUnknown =
            crag.copy(vie = crag.vie + Via(id = 9, nome = "Boh", grado = "?", settoreId = 1))

        val names = filterVie(
            withUnknown,
            RouteFilters(band = GradeBand.UpTo5)
        ).flatMap { s -> s.vie.map { it.nome } }

        assertTrue("Boh" !in names)
    }

    // ── reduce ──

    @Test
    fun `filter options list the sectors with routes and the known styles`() {
        val withEmptySector = crag.copy(settori = crag.settori + settore(3, "Vuoto"))

        val state = reduceRouteList(
            withEmptySector,
            RouteFilters(),
            RoutesStatus.Idle
        ) as RouteListUiState.Content

        assertEquals(listOf(1L, 2L), state.settori.map { it.id })
        assertEquals(listOf(ViaStyle.Sport, ViaStyle.Trad), state.styles)
        assertTrue(state.hasRoutes)
    }

    @Test
    fun `missing crag is NotFound`() {
        assertEquals(
            RouteListUiState.NotFound,
            reduceRouteList(null, RouteFilters(), RoutesStatus.Idle)
        )
    }

    // ── ViewModel ──

    private val testDispatcher = UnconfinedTestDispatcher()
    private val refreshDetails = mockk<RefreshCragDetailsUseCase>()
    private val getFalesia =
        mockk<GetFalesiaWithVieUseCase>().also { every { it("arco") } returns MutableStateFlow(crag) }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery {
            refreshDetails(
                "arco",
                false
            )
        } returns Either.Right(RefreshOutcome.AlreadyUpToDate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `intents update the filters and clear resets them`() = runTest {
        val viewModel = RouteListViewModel("arco", getFalesia, refreshDetails)
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }
        fun filters() = (viewModel.uiState.value as RouteListUiState.Content).filters

        viewModel.onIntent(RouteListIntent.QueryChanged("so"))
        viewModel.onIntent(RouteListIntent.BandSelected(GradeBand.Six))
        viewModel.onIntent(RouteListIntent.SectorSelected(1))
        viewModel.onIntent(RouteListIntent.StyleSelected(ViaStyle.Sport))
        assertEquals(RouteFilters("so", GradeBand.Six, 1, ViaStyle.Sport), filters())
        assertTrue(filters().isActive)

        viewModel.onIntent(RouteListIntent.ClearFilters)
        assertEquals(RouteFilters(), filters())
        assertFalse(filters().isActive)
    }

    @Test
    fun `a failed download without routes shows the error`() = runTest {
        coEvery { refreshDetails("arco", false) } returns Either.Left(
            SyncError.NetworkFailure(
                RuntimeException()
            )
        )
        every { getFalesia("arco") } returns MutableStateFlow(crag.copy(vie = emptyList()))
        val viewModel = RouteListViewModel("arco", getFalesia, refreshDetails)
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        assertTrue((viewModel.uiState.value as RouteListUiState.Content).routesError)
    }

    // ── Helpers ──

    private fun settore(id: Long, nome: String) = Settore(
        id = id,
        nome = nome,
        numeroVie = 0,
        gradoMin = "",
        gradoMax = "",
        length = "",
        approachMinMinutes = null,
        approachMaxMinutes = null,
        exposures = emptyList(),
        rating = null,
        ratingCount = 0,
        style = ViaStyle.Sport,
        location = null,
        linkDettagli = "",
    )

    private fun falesia() = Falesia(
        id = "arco",
        nome = "Arco",
        zona = "Trentino",
        pictureUrl = null,
        latitude = null,
        longitude = null,
        numeroVie = 3,
        linkDettagli = "",
        lastUpdated = 0L,
        settori = listOf(settore(1, "Placche"), settore(2, "Strapiombi")),
        vie = listOf(
            Via(id = 1, nome = "Sole", grado = "6a+", settoreId = 1, style = ViaStyle.Sport),
            Via(id = 2, nome = "Luna", grado = "7b", settoreId = 1, style = ViaStyle.Sport),
            Via(id = 3, nome = "Stella", grado = "6a", settoreId = 2, style = ViaStyle.Trad),
        ),
    )
}
