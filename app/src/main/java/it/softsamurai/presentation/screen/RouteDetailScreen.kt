package it.softsamurai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.PillShape
import it.softsamurai.core.ui.component.FalesiaIconButton
import it.softsamurai.core.ui.component.FalesiaTopAppBar
import it.softsamurai.core.ui.component.InfoTileStart
import it.softsamurai.core.ui.component.RouteListItem
import it.softsamurai.core.ui.component.SectionHeader
import it.softsamurai.core.ui.component.TopBarNavigation
import it.softsamurai.core.ui.component.TopoBadge
import it.softsamurai.core.ui.component.TopoSpec
import it.softsamurai.core.ui.component.TopoView
import it.softsamurai.core.ui.component.TopoZoomBox
import it.softsamurai.domain.model.RouteDetail
import it.softsamurai.domain.model.Topo
import it.softsamurai.domain.model.TopoLine
import it.softsamurai.domain.model.TopoRect
import it.softsamurai.domain.model.vieOfSettore
import it.softsamurai.presentation.RouteDetailIntent
import it.softsamurai.presentation.RouteDetailUiState
import it.softsamurai.presentation.RouteDetailViewModel
import it.softsamurai.presentation.reduceRouteDetail
import it.softsamurai.core.ui.R as CoreR

const val TAG_ROUTE_DETAIL = "route_detail"
const val TAG_TOPO = "topo"
const val TAG_TOPO_MISSING = "topo_missing"
const val TAG_TOPO_ZOOM = "topo_zoom"
const val TAG_TOPO_FULL_SCREEN = "topo_full_screen"
const val TAG_SECTOR_ROUTE = "sector_route"

/** Placeholder ratio when the sector has no topo: the site's photos are about 4:3. */
private const val MISSING_TOPO_RATIO = 4f / 3f

@Composable
fun RouteDetailRoute(
    falesiaId: String,
    viaId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouteDetailViewModel =
        hiltViewModel<RouteDetailViewModel, RouteDetailViewModel.Factory>(key = "$falesiaId/$viaId") {
            it.create(falesiaId, viaId)
        },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RouteDetailScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * Route detail (screens/RouteTopo.dc.html): the sector's topo with the route highlighted, its facts and the
 * sector's routes, numbered as on the topo; a route in the list or a line on the topo selects that route.
 * The topo keeps its proportions (no 3:4 crop) and opens full screen with pinch-zoom. Left out of the design:
 * Share, More, Beta & Notes, Log Ascent and Save List (read-only app, no such data on the site); the Gear tile
 * is the Rating.
 */
@Composable
fun RouteDetailScreen(
    uiState: RouteDetailUiState,
    onIntent: (RouteDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    initialFullScreen: Boolean = false,
) {
    val content = uiState as? RouteDetailUiState.Content
    var fullScreen by rememberSaveable { mutableStateOf(initialFullScreen) }
    Scaffold(
        modifier = modifier.testTag(TAG_ROUTE_DETAIL),
        containerColor = FalesiaTheme.colors.bg,
        topBar = {
            FalesiaTopAppBar(
                title = content?.via?.nome ?: stringResource(R.string.route_detail_title),
                subtitle = content?.let {
                    if (it.settoreNome.isBlank()) it.cragNome
                    else stringResource(R.string.route_subtitle, it.settoreNome, it.cragNome)
                },
                navigation = TopBarNavigation.Back(stringResource(R.string.cd_back), onBack),
            )
        },
    ) { innerPadding ->
        when (uiState) {
            RouteDetailUiState.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            RouteDetailUiState.NotFound -> Message(
                text = stringResource(R.string.route_not_found),
                modifier = Modifier.padding(innerPadding),
            )

            is RouteDetailUiState.Content -> RouteDetailContent(
                content = uiState,
                onIntent = onIntent,
                onZoom = { fullScreen = true },
                innerPadding = innerPadding,
            )
        }
    }
    val topo = content?.topo
    if (fullScreen && topo != null) {
        TopoFullScreen(content, topo, onIntent, onDismiss = { fullScreen = false })
    }
}

@Composable
private fun RouteDetailContent(
    content: RouteDetailUiState.Content,
    onIntent: (RouteDetailIntent) -> Unit,
    onZoom: () -> Unit,
    innerPadding: PaddingValues,
) {
    val spacing = FalesiaTheme.spacing
    LazyColumn(
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() + spacing.space6,
        ),
    ) {
        item(key = "topo") { TopoSection(content, onIntent, onZoom) }
        item(key = "tiles") { RouteTiles(content, Modifier.padding(spacing.space4)) }
        item(key = "sector_header") {
            SectionHeader(
                title = stringResource(R.string.sector_routes),
                titleStyle = FalesiaTheme.typography.cardTitle,
                modifier = Modifier.padding(
                    start = spacing.space4,
                    end = spacing.space4,
                    top = spacing.space2,
                    bottom = spacing.space3,
                ),
            )
        }
        items(content.sectorRoutes, key = { "via_${it.via.id}" }) { route ->
            RouteListItem(
                name = route.via.nome,
                grade = route.via.grado.ifBlank { stringResource(R.string.value_unknown) },
                gradeLabel = stringResource(R.string.label_grade),
                length = route.via.length.ifBlank { null },
                rating = route.via.rating?.let(::ratingText),
                style = route.via.style.label(),
                number = route.number.toString(),
                selected = route.selected,
                onClick = { onIntent(RouteDetailIntent.SelectRoute(route.via.id)) },
                modifier = Modifier
                    .padding(horizontal = spacing.space4, vertical = spacing.space1Half)
                    .testTag(TAG_SECTOR_ROUTE),
            )
        }
    }
}

/** The topo with the grade badge and the zoom button over it (`fm-topo-bar`), or a placeholder without topo. */
@Composable
private fun TopoSection(
    content: RouteDetailUiState.Content,
    onIntent: (RouteDetailIntent) -> Unit,
    onZoom: () -> Unit,
) {
    val spacing = FalesiaTheme.spacing
    val topo = content.topo
    if (topo == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MISSING_TOPO_RATIO)
                .background(FalesiaTheme.colors.primary10)
                .testTag(TAG_TOPO_MISSING),
            contentAlignment = Alignment.Center,
        ) {
            Message(stringResource(R.string.topo_missing))
        }
        return
    }
    Box {
        RouteTopo(content, topo, onIntent, Modifier.fillMaxWidth())
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(spacing.space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            TopoBadge(
                grade = content.via.grado.ifBlank { stringResource(R.string.value_unknown) },
                label = stringResource(R.string.topo_label),
            )
            FalesiaIconButton(
                icon = CoreR.drawable.ic_zoom_in,
                contentDescription = stringResource(R.string.cd_zoom_topo),
                onClick = onZoom,
                filled = true,
                modifier = Modifier
                    .shadow(FalesiaTheme.spacing.space2, PillShape)
                    .size(FalesiaTheme.size.iconButtonLg)
                    .testTag(TAG_TOPO_ZOOM),
            )
        }
    }
}

/**
 * The topo of the selected route. The selection is handed over as a state read in the draw phase: picking
 * another route redraws the lines without rebuilding the topo.
 */
@Composable
private fun RouteTopo(
    content: RouteDetailUiState.Content,
    topo: TopoSpec,
    onIntent: (RouteDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedId by rememberUpdatedState(content.via.id)
    TopoView(
        spec = topo,
        selectedId = { selectedId },
        contentDescription = stringResource(
            R.string.cd_topo,
            content.settoreNome.ifBlank { content.cragNome },
            content.number?.toString() ?: content.via.nome,
        ),
        onLineClick = { onIntent(RouteDetailIntent.SelectRoute(it)) },
        modifier = modifier.testTag(TAG_TOPO),
    ) { imageModifier ->
        AsyncImage(
            model = content.topoImageUrl,
            contentDescription = null,
            placeholder = ColorPainter(FalesiaTheme.colors.primary10),
            error = ColorPainter(FalesiaTheme.colors.primary10),
            fallback = ColorPainter(FalesiaTheme.colors.primary10),
            contentScale = ContentScale.FillBounds,
            modifier = imageModifier,
        )
    }
}

/** Grade, length, style and rating, two by two (`fm-tiles-2` of `fm-tile-start`). */
@Composable
private fun RouteTiles(content: RouteDetailUiState.Content, modifier: Modifier = Modifier) {
    val via = content.via
    val unknown = stringResource(R.string.value_unknown)
    val spacing = FalesiaTheme.spacing
    val rating = via.rating?.let {
        pluralStringResource(
            R.plurals.crag_rating_badge,
            via.ratingCount,
            ratingText(it),
            via.ratingCount
        )
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space3)) {
            InfoTileStart(
                label = stringResource(R.string.label_grade),
                value = via.grado.ifBlank { unknown },
                modifier = Modifier.weight(1f),
            )
            InfoTileStart(
                label = stringResource(R.string.info_length),
                value = via.length.ifBlank { unknown },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space3)) {
            InfoTileStart(
                label = stringResource(R.string.info_style),
                value = via.style.label() ?: unknown,
                modifier = Modifier.weight(1f),
            )
            InfoTileStart(
                label = stringResource(R.string.info_rating),
                value = rating ?: unknown,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** The topo full screen on black, with pinch-zoom and pan; lines can still be tapped. */
@Composable
private fun TopoFullScreen(
    content: RouteDetailUiState.Content,
    topo: TopoSpec,
    onIntent: (RouteDetailIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag(TAG_TOPO_FULL_SCREEN),
        ) {
            TopoZoomBox(Modifier.fillMaxSize()) {
                RouteTopo(content, topo, onIntent, Modifier.fillMaxWidth())
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(FalesiaTheme.spacing.space4),
            ) {
                FalesiaIconButton(
                    icon = CoreR.drawable.ic_close,
                    contentDescription = stringResource(R.string.cd_close),
                    onClick = onDismiss,
                    filled = true,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
                TopoBadge(
                    grade = content.via.grado.ifBlank { stringResource(R.string.value_unknown) },
                    label = content.via.nome,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }
        }
    }
}

// ──────────────────────── Previews ────────────────────────

/** Topo of sector 12 of [previewCrag]: real paths from falesiaonline.it, route 3 with two pitches. */
internal val previewTopo = Topo(
    settoreId = 12,
    imageUrl = null,
    frame = TopoRect(0.0, 0.0, 936.141, 528.686),
    image = TopoRect(0.0, 0.0, 936.141, 528.686),
    lines = listOf(
        TopoLine(
            viaId = 2,
            segments = listOf(
                "M93.57,239.543c0,0-4.599,25.596-8,40.772c-4.743,21.164-11.5,58.728-17.5,79.728s-19,98.5-19,98.5",
            ),
        ),
        TopoLine(
            viaId = 3,
            segments = listOf(
                "M291.07,518.411c0,0,33.5-86.368,42-109.868s18.169-54.724,22.889-76.904c5.291-24.868,8.758-53.991," +
                        "10.111-75.096",
                "M366.07,256.543c0.765-11.923,0.221-22.779-1.043-32.969c-1.264-10.19-3.248-19.714-5.364-28.973" +
                        "c-4.024-17.608-7.093-33.558-14.093-46.058s-12.5-17-12.5-17",
            ),
        ),
    ),
)

internal val previewRouteDetail = RouteDetail(
    crag = previewCrag,
    via = previewCrag.vie.first { it.id == 2L },
    settore = previewSettori.first { it.id == 12L },
    sectorVie = previewCrag.vieOfSettore(12),
    topo = previewTopo,
)

@PreviewLightDark
@Composable
private fun RouteDetailScreenPreview() {
    AppTheme {
        RouteDetailScreen(uiState = reduceRouteDetail(previewRouteDetail, 2), onIntent = {})
    }
}

@PreviewLightDark
@Composable
private fun RouteDetailScreenNoTopoPreview() {
    AppTheme {
        RouteDetailScreen(
            uiState = reduceRouteDetail(previewRouteDetail.copy(topo = null), 3),
            onIntent = {})
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun RouteDetailScreenLargeFontPreview() {
    AppTheme {
        RouteDetailScreen(uiState = reduceRouteDetail(previewRouteDetail, 3), onIntent = {})
    }
}
