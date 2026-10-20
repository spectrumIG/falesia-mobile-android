package it.softsamurai.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.component.FalesiaFilterChipRow
import it.softsamurai.core.ui.component.FalesiaIconButton
import it.softsamurai.core.ui.component.FalesiaPrimaryButton
import it.softsamurai.core.ui.component.FalesiaSearchField
import it.softsamurai.core.ui.component.FalesiaTopAppBar
import it.softsamurai.core.ui.component.SectionHeader
import it.softsamurai.core.ui.component.TopBarAction
import it.softsamurai.core.ui.component.TopBarNavigation
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.gradeRange
import it.softsamurai.presentation.DiscoverEvent
import it.softsamurai.presentation.FalesiaItemUiState
import it.softsamurai.presentation.FalesiaItemViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Locale
import it.softsamurai.core.ui.R as CoreUiR

const val TAG_PULL_TO_REFRESH = "pull_to_refresh"
const val TAG_CRAG_CARD = "crag_card"
const val TAG_CRAG_LOCATION = "crag_location"
const val TAG_SYNC_FAILED = "sync_failed"

private const val CRAG_PHOTO_ASPECT_RATIO = 16f / 9f
private val LocationIconSize = 14.dp
private val CardRestElevation = 1.dp

@Composable
fun DiscoverRoute(
    onCragClick: (falesiaId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FalesiaItemViewModel = hiltViewModel<FalesiaItemViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DiscoverScreen(
        uiState = uiState,
        events = viewModel.events,
        onRefresh = viewModel::onRefresh,
        onCragClick = onCragClick,
        manualDownloads = viewModel.manualDownloads,
        modifier = modifier,
    )
}

/**
 * Discover: crag list under a sticky header with search and filters (screens/Main.dc.html). With
 * [manualDownloads] (debug) there is no pull-to-refresh: the top bar download button calls [onRefresh].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    uiState: FalesiaItemUiState,
    modifier: Modifier = Modifier,
    events: Flow<DiscoverEvent> = emptyFlow(),
    onRefresh: () -> Unit = {},
    onCragClick: (falesiaId: String) -> Unit = {},
    manualDownloads: Boolean = false,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val syncErrorMessage = stringResource(R.string.sync_error_snackbar)
    val upToDateMessage = stringResource(R.string.refresh_up_to_date)

    LaunchedEffect(events) {
        events.collect { event ->
            val message = when (event) {
                DiscoverEvent.SyncFailed -> syncErrorMessage
                DiscoverEvent.AlreadyUpToDate -> upToDateMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = FalesiaTheme.colors.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DiscoverHeader(
                downloadAction = if (manualDownloads) {
                    TopBarAction(
                        icon = R.drawable.ic_cloud_download,
                        contentDescription = stringResource(R.string.cd_download),
                        onClick = onRefresh,
                        isLoading = (uiState as? FalesiaItemUiState.Success)?.isRefreshing == true,
                    )
                } else {
                    null
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is FalesiaItemUiState.Loading -> LoadingContent(innerPadding)
            is FalesiaItemUiState.SyncFailed -> SyncFailedContent(innerPadding, onRetry = onRefresh)
            is FalesiaItemUiState.Error -> ErrorContent(uiState, innerPadding)
            is FalesiaItemUiState.Success -> if (manualDownloads) {
                Box(Modifier.padding(top = innerPadding.calculateTopPadding())) {
                    CragListContent(uiState.data, innerPadding, onCragClick)
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .testTag(TAG_PULL_TO_REFRESH),
                ) {
                    CragListContent(uiState.data, innerPadding, onCragClick)
                }
            }
        }
    }
}

@Composable
private fun DiscoverHeader(downloadAction: TopBarAction? = null) {
    val spacing = FalesiaTheme.spacing
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filters = listOf(
        stringResource(R.string.filter_style),
        stringResource(R.string.filter_grade_range),
        stringResource(R.string.filter_distance),
    )
    var selectedFilter by rememberSaveable { mutableStateOf<String?>(null) }

    FalesiaTopAppBar(
        title = stringResource(R.string.crag_atlas),
        navigation = TopBarNavigation.Menu(stringResource(R.string.cd_menu)),
        action = downloadAction,
        containerColor = FalesiaTheme.colors.bgGlass95,
    ) {
        FalesiaSearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = stringResource(R.string.search_placeholder),
            modifier = Modifier.padding(
                start = spacing.space4,
                end = spacing.space4,
                bottom = spacing.space4
            ),
        )
        // The first chip is selected until the user picks another one (design component FilterChip).
        FalesiaFilterChipRow(
            labels = filters,
            selected = selectedFilter ?: filters.first(),
            onSelect = { selectedFilter = it },
            modifier = Modifier.padding(bottom = spacing.space4),
        )
    }
}

@Composable
private fun CragListContent(
    crags: List<Falesia>,
    innerPadding: PaddingValues,
    onCragClick: (falesiaId: String) -> Unit,
) {
    val spacing = FalesiaTheme.spacing
    LazyColumn(
        contentPadding = PaddingValues(
            top = spacing.space6,
            bottom = innerPadding.calculateBottomPadding() + spacing.space10,
            start = spacing.space4,
            end = spacing.space4,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.space6),
    ) {
        item {
            SectionHeader(
                title = stringResource(R.string.nearby_crags),
                actionLabel = stringResource(R.string.map_view),
            )
        }
        items(crags, key = { it.id }) { crag ->
            CragCard(crag = crag, onClick = { onCragClick(crag.id) })
        }
    }
}

@Composable
private fun LoadingContent(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/** No saved crags and the download failed (site down, no network): explain it and offer Retry. */
@Composable
private fun SyncFailedContent(innerPadding: PaddingValues, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(FalesiaTheme.spacing.space6)
            .testTag(TAG_SYNC_FAILED),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            FalesiaTheme.spacing.space4,
            Alignment.CenterVertically
        ),
    ) {
        Text(
            text = stringResource(R.string.crags_sync_failed),
            style = FalesiaTheme.typography.body,
            color = FalesiaTheme.colors.inkMuted,
            textAlign = TextAlign.Center,
        )
        FalesiaPrimaryButton(text = stringResource(R.string.retry), onClick = onRetry)
    }
}

@Composable
private fun ErrorContent(uiState: FalesiaItemUiState.Error, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(FalesiaTheme.spacing.space4),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = uiState.message,
            color = MaterialTheme.colorScheme.error,
            style = FalesiaTheme.typography.body,
        )
    }
}

/**
 * A crag in the Discover list (design component `CragCard`): 16:9 photo, name, location, two stats and the
 * round chevron. The whole card opens the crag; the chevron is its visible affordance.
 */
@Composable
fun CragCard(
    crag: Falesia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FalesiaTheme.colors
    val spacing = FalesiaTheme.spacing
    // shadow-sm only where the card is opaque (light); under the dark translucent fill it would show through.
    val elevation = if (colors.surfaceCard.alpha == 1f) CardRestElevation else 0.dp

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(TAG_CRAG_CARD),
        shape = RoundedCornerShape(FalesiaTheme.radius.xl),
        border = BorderStroke(FalesiaTheme.size.hairline, colors.line),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceCard,
            contentColor = colors.ink
        ),
    ) {
        AsyncImage(
            model = crag.pictureUrl,
            contentDescription = stringResource(R.string.cd_crag_photo, crag.nome),
            placeholder = ColorPainter(colors.primary10),
            error = ColorPainter(colors.primary10),
            fallback = ColorPainter(colors.primary10),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CRAG_PHOTO_ASPECT_RATIO),
            contentScale = ContentScale.Crop,
        )

        Column(modifier = Modifier.padding(spacing.space5)) {
            Text(
                text = crag.nome,
                style = FalesiaTheme.typography.cardTitle,
                color = colors.ink,
            )
            // The scraper does not provide the zone yet: no location line rather than an orphan pin.
            if (crag.zona.isNotBlank()) {
                Row(
                    modifier = Modifier.testTag(TAG_CRAG_LOCATION),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space1),
                ) {
                    Icon(
                        painter = painterResource(CoreUiR.drawable.ic_location_on),
                        contentDescription = null,
                        modifier = Modifier.size(LocationIconSize),
                        tint = colors.inkMuted,
                    )
                    Text(
                        text = crag.zona,
                        style = FalesiaTheme.typography.bodySm,
                        color = colors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = spacing.space6, bottom = spacing.space4),
                thickness = FalesiaTheme.size.hairline,
                color = colors.lineDivider,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space4),
            ) {
                StatColumn(
                    label = stringResource(R.string.label_routes),
                    value = crag.numeroVie.toString(),
                )
                crag.gradeRange()?.let { range ->
                    StatColumn(
                        label = stringResource(R.string.label_grades),
                        value = with(range) { if (start == endInclusive) "$start" else "$start–$endInclusive" },
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    FalesiaIconButton(
                        icon = CoreUiR.drawable.ic_chevron_right,
                        contentDescription = stringResource(R.string.cd_view_crag, crag.nome),
                        onClick = onClick,
                        filled = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = FalesiaTheme.typography.label,
            color = FalesiaTheme.colors.inkMuted,
        )
        Text(
            text = value,
            style = FalesiaTheme.typography.stat,
            color = FalesiaTheme.colors.ink,
        )
    }
}

// Keep legacy composables — wired to DiscoverRoute
@Composable
fun FalesiaItemRoute(
    modifier: Modifier = Modifier,
    onCragClick: (falesiaId: String) -> Unit = {},
    viewModel: FalesiaItemViewModel = hiltViewModel(),
) = DiscoverRoute(onCragClick, modifier, viewModel)

// ──────────────────────── Previews ────────────────────────

private val previewCrags = listOf(
    Falesia(
        id = "monte-arrubiu",
        nome = "Monte Arrubiu",
        zona = "Sardegna, Ogliastra",
        pictureUrl = null,
        latitude = 39.5,
        longitude = 9.5,
        numeroVie = 42,
        linkDettagli = "https://example.com/monte-arrubiu",
        vie = listOf(
            Via(nome = "Via del Sole", grado = "6a"),
            Via(nome = "Placca Rossa", grado = "7b+"),
        ),
        lastUpdated = 0L,
    ),
    Falesia(
        id = "finale-ligure",
        nome = "Finale Ligure",
        zona = "Liguria",
        pictureUrl = null,
        latitude = 44.16,
        longitude = 8.34,
        numeroVie = 128,
        linkDettagli = "https://example.com/finale",
        vie = listOf(Via(nome = "Spacca Tutto", grado = "8a")),
        lastUpdated = 0L,
    ),
    Falesia(
        id = "arco",
        nome = "Arco",
        zona = "Trentino-Alto Adige",
        pictureUrl = null,
        latitude = 45.92,
        longitude = 10.88,
        numeroVie = 320,
        linkDettagli = "https://example.com/arco",
        vie = emptyList(),
        lastUpdated = 0L,
    ),
)

@PreviewLightDark
@Composable
private fun DiscoverScreenLoadingPreview() {
    AppTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Loading)
    }
}

@PreviewLightDark
@Composable
private fun DiscoverScreenSyncFailedPreview() {
    AppTheme { DiscoverScreen(uiState = FalesiaItemUiState.SyncFailed) }
}

@PreviewLightDark
@Composable
private fun DiscoverScreenErrorPreview() {
    AppTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Error(message = "Network error: unable to reach server"))
    }
}

@PreviewLightDark
@Composable
private fun DiscoverScreenSuccessPreview() {
    AppTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }
}

@Preview(name = "Success — refreshing", showBackground = true, device = Devices.PHONE)
@Composable
private fun DiscoverScreenRefreshingPreview() {
    AppTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags, isRefreshing = true))
    }
}

@Preview(
    name = "Success — tablet, dark",
    showBackground = true,
    device = Devices.TABLET,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DiscoverScreenTabletPreview() {
    AppTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }
}

@Preview(name = "Success — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun DiscoverScreenLargeFontPreview() {
    AppTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }
}

@PreviewLightDark
@Composable
private fun CragCardNoViePreview() {
    AppTheme {
        CragCard(
            crag = previewCrags.last(),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun CragCardWithGradePreview() {
    AppTheme {
        CragCard(
            crag = previewCrags.first(),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
