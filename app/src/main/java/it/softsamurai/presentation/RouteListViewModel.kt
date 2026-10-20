package it.softsamurai.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.optics.optics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.FrenchGrade
import it.softsamurai.domain.model.GradeBand
import it.softsamurai.domain.model.Settore
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.domain.sync.DownloadMode
import it.softsamurai.domain.usecase.GetDownloadModeUseCase
import it.softsamurai.domain.usecase.GetFalesiaWithVieUseCase
import it.softsamurai.domain.usecase.RefreshCragDetailsUseCase
import it.softsamurai.domain.usecase.RefreshOutcome
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the route list shows: free text (name or grade) and three optional filters. */
@optics
data class RouteFilters(
    val query: String = "",
    val band: GradeBand? = null,
    val settoreId: Long? = null,
    val style: ViaStyle? = null,
) {
    val isActive: Boolean get() = query.isNotBlank() || band != null || settoreId != null || style != null

    companion object
}

/** The routes of one sector that pass the filters. */
data class RouteSection(val settore: Settore, val vie: List<Via>)

sealed interface RouteListIntent {
    data class QueryChanged(val query: String) : RouteListIntent
    data class BandSelected(val band: GradeBand?) : RouteListIntent
    data class SectorSelected(val settoreId: Long?) : RouteListIntent
    data class StyleSelected(val style: ViaStyle?) : RouteListIntent
    data object ClearFilters : RouteListIntent
    data object Retry : RouteListIntent

    /** Debug download button: download the routes again unless they are very recent. */
    data object Download : RouteListIntent
}

sealed interface RouteListEffect {
    data object SyncFailed : RouteListEffect
    data object AlreadyUpToDate : RouteListEffect
}

@Immutable
sealed interface RouteListUiState {
    data object Loading : RouteListUiState
    data object NotFound : RouteListUiState

    data class Content(
        val nome: String,
        val zona: String,
        val filters: RouteFilters,
        val sections: List<RouteSection>,
        /** Sectors and styles the crag has: the options of the filters. */
        val settori: List<Settore>,
        val styles: List<ViaStyle>,
        val hasRoutes: Boolean,
        val routesLoading: Boolean = false,
        val routesError: Boolean = false,
        /** Debug: the routes were never downloaded and only the download button fetches them. */
        val routesNotDownloaded: Boolean = false,
        val isDownloading: Boolean = false,
    ) : RouteListUiState
}

/**
 * Routes of [crag] that match [filters], grouped by sector in the site's order. The query matches the
 * route name (any part, ignoring case) or the start of the grade (`6a` finds `6a` and `6a+`).
 */
internal fun filterVie(crag: Falesia, filters: RouteFilters): List<RouteSection> {
    val query = filters.query.trim()
    val bySettore = crag.vie.groupBy { it.settoreId }
    return crag.settori
        .filter { filters.settoreId == null || it.id == filters.settoreId }
        .map { settore ->
            RouteSection(
                settore = settore,
                vie = bySettore[settore.id].orEmpty().filter { via ->
                    (query.isEmpty() || via.nome.contains(query, ignoreCase = true) ||
                            via.grado.startsWith(query, ignoreCase = true)) &&
                            (filters.band == null || FrenchGrade.parse(via.grado)
                                ?.let(GradeBand::of) == filters.band) &&
                            (filters.style == null || via.style == filters.style)
                },
            )
        }
        .filter { it.vie.isNotEmpty() }
}

internal fun reduceRouteList(
    crag: Falesia?,
    filters: RouteFilters,
    status: RoutesStatus,
    manualDownloads: Boolean = false,
    isDownloading: Boolean = false,
): RouteListUiState = when (crag) {
    null -> RouteListUiState.NotFound
    else -> RouteListUiState.Content(
        nome = crag.nome,
        zona = crag.zona,
        filters = filters,
        sections = filterVie(crag, filters),
        settori = crag.settori.filter { settore -> crag.vie.any { it.settoreId == settore.id } },
        styles = crag.vie.map { it.style }.filter { it != ViaStyle.Unknown }.distinct().sorted(),
        hasRoutes = crag.vie.isNotEmpty(),
        routesLoading = status == RoutesStatus.Loading && crag.vie.isEmpty(),
        routesError = status == RoutesStatus.Failed && crag.vie.isEmpty(),
        routesNotDownloaded = manualDownloads && crag.vie.isEmpty() && crag.vieUpdatedAt == null,
        isDownloading = isDownloading,
    )
}

/** Route list of a crag (screens/RouteList.dc.html), with search and filters. */
@HiltViewModel(assistedFactory = RouteListViewModel.Factory::class)
class RouteListViewModel @AssistedInject constructor(
    @Assisted private val falesiaId: String,
    getFalesiaWithVie: GetFalesiaWithVieUseCase,
    private val refreshCragDetails: RefreshCragDetailsUseCase,
    getDownloadMode: GetDownloadModeUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(falesiaId: String): RouteListViewModel
    }

    /** Debug builds: no download on open, a download button instead (see `DownloadMode`). */
    val manualDownloads: Boolean = getDownloadMode() == DownloadMode.Manual

    private val filters = MutableStateFlow(RouteFilters())
    private val routesStatus = MutableStateFlow(RoutesStatus.Idle)
    private val isDownloading = MutableStateFlow(false)
    private val _effects = Channel<RouteListEffect>(Channel.BUFFERED)

    /** One-shot UI effects of the download button (snackbars). */
    val effects: Flow<RouteListEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<RouteListUiState> =
        combine(getFalesiaWithVie(falesiaId), filters, routesStatus, isDownloading) { crag, filters, status, downloading ->
            reduceRouteList(crag, filters, status, manualDownloads, downloading)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RouteListUiState.Loading,
            )

    init {
        download()
    }

    fun onIntent(intent: RouteListIntent) {
        when (intent) {
            is RouteListIntent.QueryChanged -> filters.update {
                RouteFilters.query.set(
                    it,
                    intent.query
                )
            }

            is RouteListIntent.BandSelected -> filters.update {
                RouteFilters.band.set(
                    it,
                    intent.band
                )
            }

            is RouteListIntent.SectorSelected -> filters.update {
                RouteFilters.settoreId.set(
                    it,
                    intent.settoreId
                )
            }

            is RouteListIntent.StyleSelected -> filters.update {
                RouteFilters.style.set(
                    it,
                    intent.style
                )
            }

            RouteListIntent.ClearFilters -> filters.value = RouteFilters()
            RouteListIntent.Retry -> download()
            RouteListIntent.Download -> forceDownload()
        }
    }

    private fun forceDownload() {
        if (isDownloading.value) return
        isDownloading.value = true
        viewModelScope.launch {
            refreshCragDetails(falesiaId, force = true).fold(
                ifLeft = { _effects.send(RouteListEffect.SyncFailed) },
                ifRight = { outcome ->
                    routesStatus.value = RoutesStatus.Idle
                    if (outcome == RefreshOutcome.AlreadyUpToDate) _effects.send(RouteListEffect.AlreadyUpToDate)
                },
            )
            isDownloading.value = false
        }
    }

    /** Usually a no-op: crag details downloaded the routes already. */
    private fun download() {
        if (routesStatus.value == RoutesStatus.Loading) return
        routesStatus.value = RoutesStatus.Loading
        viewModelScope.launch {
            routesStatus.value = refreshCragDetails(falesiaId).fold(
                ifLeft = { RoutesStatus.Failed },
                ifRight = { RoutesStatus.Idle },
            )
        }
    }
}
