package it.softsamurai.presentation.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.component.CragHero
import it.softsamurai.core.ui.component.FalesiaPrimaryButton
import it.softsamurai.core.ui.component.FalesiaTopAppBar
import it.softsamurai.core.ui.component.InfoTile
import it.softsamurai.core.ui.component.RouteRow
import it.softsamurai.core.ui.component.SectionHeader
import it.softsamurai.core.ui.component.StyleTag
import it.softsamurai.core.ui.component.TopBarAction
import it.softsamurai.core.ui.component.TopBarNavigation
import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.GeoPoint
import it.softsamurai.domain.model.Settore
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.presentation.CragDetailsEffect
import it.softsamurai.presentation.CragDetailsIntent
import it.softsamurai.presentation.CragDetailsUiState
import it.softsamurai.presentation.CragDetailsViewModel
import it.softsamurai.presentation.RoutesStatus
import it.softsamurai.presentation.reduceCragDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Locale

const val TAG_CRAG_DETAILS = "crag_details"
const val TAG_FEATURED_ROUTE = "featured_route"
const val TAG_ROUTES_ERROR = "routes_error"
const val TAG_ROUTES_LOADING = "routes_loading"

@Composable
fun CragDetailsRoute(
    falesiaId: String,
    onBack: () -> Unit,
    onSeeAllRoutes: (falesiaId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CragDetailsViewModel =
        hiltViewModel<CragDetailsViewModel, CragDetailsViewModel.Factory>(key = falesiaId) {
            it.create(
                falesiaId
            )
        },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CragDetailsScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onSeeAllRoutes = onSeeAllRoutes,
        effects = viewModel.effects,
        manualDownloads = viewModel.manualDownloads,
        modifier = modifier,
    )
}

/**
 * Crag details (screens/CragDetails.dc.html), without the favourite button and the "+" FAB (read-only app).
 * With [manualDownloads] (debug) there is no pull-to-refresh: the top bar download button sends Refresh.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CragDetailsScreen(
    uiState: CragDetailsUiState,
    onIntent: (CragDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSeeAllRoutes: (falesiaId: String) -> Unit = {},
    effects: Flow<CragDetailsEffect> = emptyFlow(),
    manualDownloads: Boolean = false,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val syncFailed = stringResource(R.string.sync_error_snackbar)
    val upToDate = stringResource(R.string.refresh_up_to_date)
    val noMapsApp = stringResource(R.string.no_maps_app)
    val currentOnSeeAll by rememberUpdatedState(onSeeAllRoutes)

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                CragDetailsEffect.SyncFailed -> snackbarHostState.showSnackbar(syncFailed)
                CragDetailsEffect.AlreadyUpToDate -> snackbarHostState.showSnackbar(upToDate)
                is CragDetailsEffect.NavigateToRouteList -> currentOnSeeAll(effect.falesiaId)
                is CragDetailsEffect.OpenMaps ->
                    if (!context.openMaps(
                            effect.destination,
                            effect.label
                        )
                    ) snackbarHostState.showSnackbar(noMapsApp)
            }
        }
    }

    val title = (uiState as? CragDetailsUiState.Content)?.crag?.nome
        ?: stringResource(R.string.crag_details_title)
    Scaffold(
        modifier = modifier.testTag(TAG_CRAG_DETAILS),
        containerColor = FalesiaTheme.colors.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FalesiaTopAppBar(
                title = title,
                navigation = TopBarNavigation.Back(stringResource(R.string.cd_back), onBack),
                action = if (manualDownloads && uiState is CragDetailsUiState.Content) {
                    TopBarAction(
                        icon = R.drawable.ic_cloud_download,
                        contentDescription = stringResource(R.string.cd_download),
                        onClick = { onIntent(CragDetailsIntent.Refresh) },
                        isLoading = uiState.isRefreshing,
                    )
                } else {
                    null
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            CragDetailsUiState.Loading -> CenteredBox(innerPadding) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            CragDetailsUiState.NotFound -> CenteredBox(innerPadding) {
                Text(
                    text = stringResource(R.string.crag_not_found),
                    style = FalesiaTheme.typography.body,
                    color = FalesiaTheme.colors.inkMuted,
                    textAlign = TextAlign.Center,
                )
            }

            is CragDetailsUiState.Content -> if (manualDownloads) {
                Box(Modifier.padding(top = innerPadding.calculateTopPadding())) {
                    CragDetailsContent(uiState, onIntent, bottomPadding = innerPadding.calculateBottomPadding())
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onIntent(CragDetailsIntent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                ) {
                    CragDetailsContent(uiState, onIntent, bottomPadding = innerPadding.calculateBottomPadding())
                }
            }
        }
    }
}

@Composable
private fun CragDetailsContent(
    state: CragDetailsUiState.Content,
    onIntent: (CragDetailsIntent) -> Unit,
    bottomPadding: Dp,
) {
    val crag = state.crag
    val spacing = FalesiaTheme.spacing
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding + spacing.space6)) {
        item(key = "hero") { Hero(crag) }
        item(key = "tiles") { QuickInfo(state, Modifier.padding(spacing.space4)) }
        item(key = "about") {
            About(
                crag,
                Modifier.padding(horizontal = spacing.space4, vertical = spacing.space2)
            )
        }
        state.location?.let { location ->
            item(key = "location") {
                LocationSection(
                    state = state,
                    location = location,
                    onOpenNavigation = { onIntent(CragDetailsIntent.OpenNavigation) },
                    modifier = Modifier.padding(
                        start = spacing.space4,
                        end = spacing.space4,
                        top = spacing.space6
                    ),
                )
            }
        }
        item(key = "featured_header") {
            SectionHeader(
                title = stringResource(R.string.featured_routes),
                titleStyle = FalesiaTheme.typography.cardTitle,
                actionLabel = stringResource(R.string.see_all).uppercase(Locale.getDefault())
                    .takeIf { crag.vie.isNotEmpty() },
                onActionClick = { onIntent(CragDetailsIntent.SeeAllRoutes) },
                modifier = Modifier.padding(
                    start = spacing.space4,
                    end = spacing.space4,
                    top = spacing.space6,
                    bottom = spacing.space4,
                ),
            )
        }
        when {
            state.routesLoading -> item(key = "routes_loading") { RoutesLoading() }
            state.routesNotDownloaded -> item(key = "routes_not_downloaded") {
                Message(stringResource(R.string.routes_not_downloaded))
            }

            state.routesError -> item(key = "routes_error") {
                RoutesError(onRetry = { onIntent(CragDetailsIntent.Retry) })
            }

            state.featured.isEmpty() -> item(key = "routes_empty") {
                Message(stringResource(R.string.routes_empty))
            }

            else -> items(state.featured, key = { it.id }) { via ->
                FeaturedRoute(
                    via,
                    Modifier.padding(horizontal = spacing.space4, vertical = spacing.space1Half)
                )
            }
        }
    }
}

@Composable
private fun Hero(crag: Falesia) {
    val colors = FalesiaTheme.colors
    val badge = crag.rating?.let {
        pluralStringResource(
            R.plurals.crag_rating_badge,
            crag.ratingCount,
            ratingText(it),
            crag.ratingCount
        )
    }
    CragHero(name = crag.nome, location = crag.zona, badge = badge) {
        AsyncImage(
            model = crag.pictureUrl,
            contentDescription = stringResource(R.string.cd_crag_photo, crag.nome),
            placeholder = ColorPainter(colors.primary10),
            error = ColorPainter(colors.primary10),
            fallback = ColorPainter(colors.primary10),
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
private fun QuickInfo(state: CragDetailsUiState.Content, modifier: Modifier = Modifier) {
    val unknown = stringResource(R.string.value_unknown)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space3),
    ) {
        val tile = Modifier
            .weight(1f)
            .fillMaxHeight()
        InfoTile(
            icon = R.drawable.ic_timer_filled,
            label = stringResource(R.string.info_approach),
            value = approachText(state.approach) ?: unknown,
            modifier = tile,
        )
        InfoTile(
            icon = R.drawable.ic_wb_sunny_filled,
            label = stringResource(R.string.info_exposure),
            value = state.exposures.map { it.label() }.joinToString(", ").ifEmpty { unknown },
            modifier = tile,
        )
        InfoTile(
            icon = R.drawable.ic_altitude_filled,
            label = stringResource(R.string.label_routes),
            value = state.crag.numeroVie.toString(),
            modifier = tile,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun About(crag: Falesia, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space2)
    ) {
        Text(
            text = stringResource(R.string.about_crag),
            style = FalesiaTheme.typography.cardTitle,
            color = FalesiaTheme.colors.ink,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = crag.description.ifBlank { stringResource(R.string.about_crag_empty) },
            style = FalesiaTheme.typography.body,
            color = FalesiaTheme.colors.inkBody,
        )
        val tags = listOf(crag.rock, crag.seasons).filter { it.isNotBlank() }
        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space2),
                verticalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space2),
            ) {
                tags.forEach { StyleTag(text = it) }
            }
        }
    }
}

@Composable
private fun LocationSection(
    state: CragDetailsUiState.Content,
    location: GeoPoint,
    onOpenNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val crag = state.crag
    val parkingLabel = crag.parking?.let {
        approachText(state.approach)?.let { walk ->
            stringResource(
                R.string.map_parking_walk,
                walk
            )
        }
            ?: stringResource(R.string.map_parking)
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space3)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.location_title),
                style = FalesiaTheme.typography.cardTitle,
                color = FalesiaTheme.colors.ink,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            Text(
                text = coordinatesText(location),
                style = FalesiaTheme.typography.caption,
                color = FalesiaTheme.colors.inkMuted,
            )
        }
        CragMap(
            nome = crag.nome,
            crag = location,
            parking = crag.parking,
            settori = crag.settori.mapNotNull(Settore::location),
            parkingLabel = parkingLabel,
        )
        if (state.navigationTarget != null) {
            FalesiaPrimaryButton(
                text = stringResource(R.string.open_navigation),
                icon = R.drawable.ic_map_filled,
                onClick = onOpenNavigation,
            )
            Text(
                text = stringResource(
                    if (crag.parking != null) R.string.open_navigation_parking_hint else R.string.open_navigation_crag_hint,
                ),
                style = FalesiaTheme.typography.caption,
                color = FalesiaTheme.colors.inkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FeaturedRoute(via: Via, modifier: Modifier = Modifier) {
    RouteRow(
        grade = via.grado.ifBlank { stringResource(R.string.value_unknown) },
        name = via.nome,
        rating = via.rating,
        ratingCount = via.ratingCount,
        ratingDescription = via.rating?.let { ratingDescription(it, via.ratingCount) }.orEmpty(),
        type = via.style.label(),
        length = via.length.ifBlank { null },
        modifier = modifier.testTag(TAG_FEATURED_ROUTE),
    )
}

@Composable
private fun RoutesLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(FalesiaTheme.spacing.space4)
            .testTag(TAG_ROUTES_LOADING),
        horizontalArrangement = Arrangement.spacedBy(
            FalesiaTheme.spacing.space3,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            text = stringResource(R.string.routes_loading),
            style = FalesiaTheme.typography.bodySm,
            color = FalesiaTheme.colors.inkMuted,
        )
    }
}

@Composable
internal fun RoutesError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(FalesiaTheme.spacing.space4)
            .testTag(TAG_ROUTES_ERROR),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.routes_error),
            style = FalesiaTheme.typography.bodySm,
            color = FalesiaTheme.colors.inkMuted,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.retry),
                style = FalesiaTheme.typography.link,
                color = FalesiaTheme.colors.primaryText,
            )
        }
    }
}

@Composable
internal fun Message(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = FalesiaTheme.typography.bodySm,
        color = FalesiaTheme.colors.inkMuted,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(FalesiaTheme.spacing.space4),
    )
}

@Composable
private fun CenteredBox(innerPadding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(FalesiaTheme.spacing.space4),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/**
 * Opens a maps app on [destination] with a labelled pin (`geo:` URI, any maps app); the user starts the
 * directions from there. Returns false when no app can open it.
 */
private fun Context.openMaps(destination: GeoPoint, label: String): Boolean {
    val coordinates =
        String.format(Locale.ROOT, "%.6f,%.6f", destination.latitude, destination.longitude)
    val uri = "geo:$coordinates?q=$coordinates(${Uri.encode(label)})".toUri()
    return try {
        startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

// ──────────────────────── Previews ────────────────────────

internal val previewSettori = listOf(
    Settore(
        id = 1510,
        nome = "Pilastro Rosso",
        numeroVie = 20,
        gradoMin = "6a+",
        gradoMax = "7c+",
        length = "13–28m",
        approachMinMinutes = 6,
        approachMaxMinutes = 15,
        exposures = listOf(Exposure.NE),
        rating = 4.0,
        ratingCount = 1,
        style = ViaStyle.Sport,
        location = GeoPoint(43.8726, 10.8229),
        linkDettagli = "",
    ),
    Settore(
        id = 12,
        nome = "il Paretone (sinistra)",
        numeroVie = 20,
        gradoMin = "5a",
        gradoMax = "7b",
        length = "8–40m",
        approachMinMinutes = 12,
        approachMaxMinutes = 21,
        exposures = listOf(Exposure.SW),
        rating = 4.5,
        ratingCount = 9,
        style = ViaStyle.Sport,
        location = GeoPoint(43.8700, 10.8257),
        linkDettagli = "",
    ),
)

internal val previewCrag = Falesia(
    id = "monsummano",
    nome = "Monsummano",
    zona = "Toscana Nord Ovest, Toscana",
    pictureUrl = null,
    latitude = 43.8695,
    longitude = 10.8273,
    numeroVie = 231,
    linkDettagli = "",
    vie = listOf(
        Via(
            id = 1,
            nome = "Zuppa inglese",
            grado = "7a+",
            settoreId = 1510,
            rating = 4.5,
            ratingCount = 12,
            length = "25m",
            style = ViaStyle.Sport
        ),
        Via(
            id = 2,
            nome = "Sanguisughe",
            grado = "6c",
            settoreId = 12,
            rating = 3.0,
            ratingCount = 5,
            length = "15–20m",
            style = ViaStyle.Sport
        ),
        Via(
            id = 3, nome = "Ditta Cambi & Corsi", grado = "6a+", settoreId = 12, length = "35m",
            style = ViaStyle.Sport
        ),
    ),
    lastUpdated = 0L,
    description = "Storica falesia invernale del Pistoiese suddivisa in vari settori.",
    rating = 4.5,
    ratingCount = 15,
    rock = "Calcare",
    seasons = "Inverno, Mezze stagioni",
    parking = GeoPoint(43.8663, 10.8280),
    settori = previewSettori,
    vieUpdatedAt = 0L,
)

@PreviewLightDark
@Composable
private fun CragDetailsScreenPreview() {
    AppTheme {
        CragDetailsScreen(
            uiState = reduceCragDetails(previewCrag, RoutesStatus.Idle, false),
            onIntent = {})
    }
}

@PreviewLightDark
@Composable
private fun CragDetailsScreenRoutesErrorPreview() {
    AppTheme {
        CragDetailsScreen(
            uiState = reduceCragDetails(
                previewCrag.copy(vie = emptyList()),
                RoutesStatus.Failed,
                false
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun CragDetailsScreenLargeFontPreview() {
    AppTheme {
        CragDetailsScreen(
            uiState = reduceCragDetails(previewCrag, RoutesStatus.Loading, false),
            onIntent = {})
    }
}
