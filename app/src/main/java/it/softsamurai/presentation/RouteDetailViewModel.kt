package it.softsamurai.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.core.ui.component.TopoLineSpec
import it.softsamurai.core.ui.component.TopoSpec
import it.softsamurai.domain.model.RouteDetail
import it.softsamurai.domain.model.Topo
import it.softsamurai.domain.model.TopoRect
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.routeNumbers
import it.softsamurai.domain.usecase.GetRouteDetailUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val KEY_SELECTED_VIA = "selectedViaId"

sealed interface RouteDetailIntent {
    /** A route of the sector was tapped, in the list or on the topo: show it, on the same topo. */
    data class SelectRoute(val viaId: Long) : RouteDetailIntent
}

/** A route of the sector, with its number on the topo. */
@Immutable
data class SectorRouteUi(val via: Via, val number: Int, val selected: Boolean)

@Immutable
sealed interface RouteDetailUiState {
    data object Loading : RouteDetailUiState
    data object NotFound : RouteDetailUiState

    data class Content(
        /** The route on screen: the one opened, or the one picked on its sector's topo. */
        val via: Via,
        val number: Int?,
        val settoreNome: String,
        val cragNome: String,
        /** The sector's topo, numbered as [sectorRoutes]; null when the site has none. */
        val topo: TopoSpec?,
        val topoImageUrl: String?,
        val sectorRoutes: List<SectorRouteUi>,
    ) : RouteDetailUiState
}

/**
 * Builds the screen from the route's detail and the route picked on the topo ([selectedViaId]). A pick that is no
 * longer in the sector (the routes were downloaded again) falls back to the route that was opened.
 */
internal fun reduceRouteDetail(detail: RouteDetail?, selectedViaId: Long): RouteDetailUiState {
    if (detail == null) return RouteDetailUiState.NotFound
    val numbers = detail.sectorVie.routeNumbers()
    val via = detail.sectorVie.find { it.id == selectedViaId } ?: detail.via
    return RouteDetailUiState.Content(
        via = via,
        number = numbers[via.id],
        settoreNome = detail.settore?.nome.orEmpty(),
        cragNome = detail.crag.nome,
        topo = detail.topo?.toSpec(numbers),
        topoImageUrl = detail.topo?.imageUrl,
        sectorRoutes = detail.sectorVie.map {
            SectorRouteUi(
                it,
                numbers.getValue(it.id),
                it.id == via.id
            )
        },
    )
}

/** The topo to draw: only the lines of routes listed in the sector, each labelled with its number. */
internal fun Topo.toSpec(numbers: Map<Long, Int>): TopoSpec = TopoSpec(
    frame = frame.toRect(),
    image = image.toRect(),
    lines = lines.mapNotNull { line ->
        numbers[line.viaId]?.let {
            TopoLineSpec(
                id = line.viaId,
                segments = line.segments,
                label = it.toString()
            )
        }
    },
)

private fun TopoRect.toRect(): Rect =
    Rect(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())

/**
 * Route detail (screens/RouteTopo.dc.html): the route on its sector's topo, its facts and the sector's routes.
 * Reads the DB only: the routes and topi come with the crag's details (crag details and route list download
 * them), so this screen never downloads. The picked route survives process death (saved state).
 */
@HiltViewModel(assistedFactory = RouteDetailViewModel.Factory::class)
class RouteDetailViewModel @AssistedInject constructor(
    @Assisted private val falesiaId: String,
    @Assisted private val viaId: Long,
    private val savedStateHandle: SavedStateHandle,
    getRouteDetail: GetRouteDetailUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(falesiaId: String, viaId: Long): RouteDetailViewModel
    }

    private val selectedViaId: StateFlow<Long> =
        savedStateHandle.getStateFlow(KEY_SELECTED_VIA, viaId)

    val uiState: StateFlow<RouteDetailUiState> =
        combine(getRouteDetail(falesiaId, viaId), selectedViaId, ::reduceRouteDetail)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RouteDetailUiState.Loading,
            )

    fun onIntent(intent: RouteDetailIntent) {
        when (intent) {
            is RouteDetailIntent.SelectRoute -> savedStateHandle[KEY_SELECTED_VIA] = intent.viaId
        }
    }
}
