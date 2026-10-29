package it.softsamurai.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.GeoPoint
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.approachRange
import it.softsamurai.domain.model.featuredVie
import it.softsamurai.domain.model.location
import it.softsamurai.domain.model.mainExposures
import it.softsamurai.domain.model.navigationTarget
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
import kotlinx.coroutines.launch

/** Download state of the on-demand routes. */
enum class RoutesStatus { Idle, Loading, Failed }

sealed interface CragDetailsIntent {
    /** Pull-to-refresh or the debug download button: download the routes again unless they are very recent. */
    data object Refresh : CragDetailsIntent

    /** Try the download again after a failure. */
    data object Retry : CragDetailsIntent
    data object OpenNavigation : CragDetailsIntent
    data object SeeAllRoutes : CragDetailsIntent
    data class RouteClicked(val viaId: Long) : CragDetailsIntent
}

sealed interface CragDetailsEffect {
    data object SyncFailed : CragDetailsEffect
    data object AlreadyUpToDate : CragDetailsEffect

    /** Open the user's maps app on [destination]. */
    data class OpenMaps(val destination: GeoPoint, val label: String) : CragDetailsEffect
    data class NavigateToRouteList(val falesiaId: String) : CragDetailsEffect
    data class NavigateToRoute(val falesiaId: String, val viaId: Long) : CragDetailsEffect
}

@Immutable
sealed interface CragDetailsUiState {
    data object Loading : CragDetailsUiState
    data object NotFound : CragDetailsUiState

    data class Content(
        val crag: Falesia,
        val featured: List<Via>,
        val approach: IntRange?,
        val exposures: List<Exposure>,
        val location: GeoPoint?,
        val navigationTarget: GeoPoint?,
        val routesLoading: Boolean = false,
        /** The download failed and there are no saved routes to show instead. */
        val routesError: Boolean = false,
        /** Debug: the routes were never downloaded and only the download button fetches them. */
        val routesNotDownloaded: Boolean = false,
        val isRefreshing: Boolean = false,
    ) : CragDetailsUiState
}

/** Builds the screen state from the DB crag and the download state. */
internal fun reduceCragDetails(
    crag: Falesia?,
    status: RoutesStatus,
    isRefreshing: Boolean,
    manualDownloads: Boolean = false,
): CragDetailsUiState = when (crag) {
    null -> CragDetailsUiState.NotFound
    else -> CragDetailsUiState.Content(
        crag = crag,
        featured = crag.featuredVie(),
        approach = crag.approachRange(),
        exposures = crag.mainExposures(),
        location = crag.location(),
        navigationTarget = crag.navigationTarget(),
        routesLoading = status == RoutesStatus.Loading && crag.vie.isEmpty(),
        routesError = status == RoutesStatus.Failed && crag.vie.isEmpty(),
        routesNotDownloaded = manualDownloads && crag.vie.isEmpty() && crag.vieUpdatedAt == null,
        isRefreshing = isRefreshing,
    )
}

/**
 * Crag details (screens/CragDetails.dc.html). Shows the crag from the DB and, on open, downloads its routes
 * and parking when missing or stale ([RefreshCragDetailsUseCase]): the DB flow then updates the screen.
 */
@HiltViewModel(assistedFactory = CragDetailsViewModel.Factory::class)
class CragDetailsViewModel @AssistedInject constructor(
    @Assisted private val falesiaId: String,
    getFalesiaWithVie: GetFalesiaWithVieUseCase,
    private val refreshCragDetails: RefreshCragDetailsUseCase,
    getDownloadMode: GetDownloadModeUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(falesiaId: String): CragDetailsViewModel
    }

    /** Debug builds: no download on open, no pull-to-refresh, a download button instead (see `DownloadMode`). */
    val manualDownloads: Boolean = getDownloadMode() == DownloadMode.Manual

    private val routesStatus = MutableStateFlow(RoutesStatus.Idle)
    private val isRefreshing = MutableStateFlow(false)
    private val _effects = Channel<CragDetailsEffect>(Channel.BUFFERED)

    /** One-shot UI effects: snackbars, maps app, navigation. */
    val effects: Flow<CragDetailsEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<CragDetailsUiState> =
        combine(getFalesiaWithVie(falesiaId), routesStatus, isRefreshing) { crag, status, refreshing ->
            reduceCragDetails(crag, status, refreshing, manualDownloads)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CragDetailsUiState.Loading,
            )

    init {
        download()
    }

    fun onIntent(intent: CragDetailsIntent) {
        when (intent) {
            CragDetailsIntent.Refresh -> refresh()
            CragDetailsIntent.Retry -> download()
            CragDetailsIntent.OpenNavigation -> openNavigation()
            CragDetailsIntent.SeeAllRoutes -> send(CragDetailsEffect.NavigateToRouteList(falesiaId))
            is CragDetailsIntent.RouteClicked -> send(
                CragDetailsEffect.NavigateToRoute(
                    falesiaId,
                    intent.viaId
                )
            )
        }
    }

    /** Download on open: silent, a failure only shows when there is nothing saved. */
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

    private fun refresh() {
        if (isRefreshing.value) return
        isRefreshing.value = true
        viewModelScope.launch {
            refreshCragDetails(falesiaId, force = true).fold(
                ifLeft = { _effects.send(CragDetailsEffect.SyncFailed) },
                ifRight = { outcome ->
                    routesStatus.value = RoutesStatus.Idle
                    if (outcome == RefreshOutcome.AlreadyUpToDate) _effects.send(CragDetailsEffect.AlreadyUpToDate)
                },
            )
            isRefreshing.value = false
        }
    }

    private fun openNavigation() {
        val content = uiState.value as? CragDetailsUiState.Content ?: return
        val destination = content.navigationTarget ?: return
        send(CragDetailsEffect.OpenMaps(destination, content.crag.nome))
    }

    private fun send(effect: CragDetailsEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
