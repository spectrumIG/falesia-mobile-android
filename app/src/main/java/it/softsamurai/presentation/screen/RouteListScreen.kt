package it.softsamurai.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.component.FalesiaFilterChip
import it.softsamurai.core.ui.component.FalesiaSearchField
import it.softsamurai.core.ui.component.FalesiaTopAppBar
import it.softsamurai.core.ui.component.RouteListItem
import it.softsamurai.core.ui.component.SectionHeader
import it.softsamurai.core.ui.component.TopBarAction
import it.softsamurai.core.ui.component.TopBarNavigation
import it.softsamurai.domain.model.GradeBand
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.presentation.RouteFilters
import it.softsamurai.presentation.RouteListEffect
import it.softsamurai.presentation.RouteListIntent
import it.softsamurai.presentation.RouteListUiState
import it.softsamurai.presentation.RouteListViewModel
import it.softsamurai.presentation.RoutesStatus
import it.softsamurai.presentation.reduceRouteList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

const val TAG_ROUTE_LIST = "route_list"
const val TAG_ROUTE_ITEM = "route_item"
const val TAG_FILTER_GRADE = "filter_grade"
const val TAG_FILTER_SECTOR = "filter_sector"
const val TAG_FILTER_STYLE = "filter_style"

@Composable
fun RouteListRoute(
    falesiaId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouteListViewModel =
        hiltViewModel<RouteListViewModel, RouteListViewModel.Factory>(key = falesiaId) {
            it.create(
                falesiaId
            )
        },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RouteListScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        effects = viewModel.effects,
        manualDownloads = viewModel.manualDownloads,
        modifier = modifier,
    )
}

/**
 * Route list of a crag (screens/RouteList.dc.html): search, filters by grade, sector and style, routes
 * grouped by sector. The design's Status filter, SENT/PROJECT tags and "+" FAB are left out (read-only app).
 * With [manualDownloads] (debug) the top bar has a download button.
 */
@Composable
fun RouteListScreen(
    uiState: RouteListUiState,
    onIntent: (RouteListIntent) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    effects: Flow<RouteListEffect> = emptyFlow(),
    manualDownloads: Boolean = false,
) {
    val content = uiState as? RouteListUiState.Content
    val snackbarHostState = remember { SnackbarHostState() }
    val syncFailed = stringResource(R.string.sync_error_snackbar)
    val upToDate = stringResource(R.string.refresh_up_to_date)
    LaunchedEffect(effects) {
        effects.collect { effect ->
            snackbarHostState.showSnackbar(
                when (effect) {
                    RouteListEffect.SyncFailed -> syncFailed
                    RouteListEffect.AlreadyUpToDate -> upToDate
                },
            )
        }
    }
    Scaffold(
        modifier = modifier.testTag(TAG_ROUTE_LIST),
        containerColor = FalesiaTheme.colors.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FalesiaTopAppBar(
                title = content?.nome ?: stringResource(R.string.crag_details_title),
                subtitle = content?.zona?.ifBlank { null },
                navigation = TopBarNavigation.Back(stringResource(R.string.cd_back), onBack),
                action = if (manualDownloads && content != null) {
                    TopBarAction(
                        icon = R.drawable.ic_cloud_download,
                        contentDescription = stringResource(R.string.cd_download),
                        onClick = { onIntent(RouteListIntent.Download) },
                        isLoading = content.isDownloading,
                    )
                } else {
                    null
                },
            ) {
                if (content != null && content.hasRoutes) RouteFiltersBar(content, onIntent)
            }
        },
    ) { innerPadding ->
        when (uiState) {
            RouteListUiState.Loading -> CenteredProgress(innerPadding)
            RouteListUiState.NotFound -> Message(
                text = stringResource(R.string.crag_not_found),
                modifier = Modifier.padding(innerPadding),
            )

            is RouteListUiState.Content -> RouteListContent(uiState, onIntent, innerPadding)
        }
    }
}

@Composable
private fun RouteFiltersBar(
    content: RouteListUiState.Content,
    onIntent: (RouteListIntent) -> Unit
) {
    val spacing = FalesiaTheme.spacing
    val filters = content.filters
    Column(
        modifier = Modifier.padding(
            start = spacing.space4,
            end = spacing.space4,
            bottom = spacing.space4
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        FalesiaSearchField(
            value = filters.query,
            onValueChange = { onIntent(RouteListIntent.QueryChanged(it)) },
            placeholder = stringResource(R.string.search_routes_placeholder),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            FilterMenu(
                label = filters.band?.label() ?: stringResource(R.string.filter_grade),
                selected = filters.band != null,
                options = GradeBand.entries.map { it.label() to it },
                onSelect = { onIntent(RouteListIntent.BandSelected(it)) },
                modifier = Modifier.testTag(TAG_FILTER_GRADE),
            )
            if (content.settori.size > 1) {
                FilterMenu(
                    label = content.settori.find { it.id == filters.settoreId }?.nome
                        ?: stringResource(R.string.filter_sector),
                    selected = filters.settoreId != null,
                    options = content.settori.map { it.nome to it.id },
                    onSelect = { onIntent(RouteListIntent.SectorSelected(it)) },
                    modifier = Modifier.testTag(TAG_FILTER_SECTOR),
                )
            }
            if (content.styles.size > 1) {
                FilterMenu(
                    label = filters.style?.label() ?: stringResource(R.string.filter_style),
                    selected = filters.style != null,
                    options = content.styles.mapNotNull { style ->
                        style.label()?.let { it to style }
                    },
                    onSelect = { onIntent(RouteListIntent.StyleSelected(it)) },
                    modifier = Modifier.testTag(TAG_FILTER_STYLE),
                )
            }
        }
    }
}

/** A filter chip that opens a menu: "All" (null) and one item per option. */
@Composable
private fun <T : Any> FilterMenu(
    label: String,
    selected: Boolean,
    options: List<Pair<String, T>>,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier = modifier) {
        FalesiaFilterChip(label = label, selected = selected, onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (listOf(stringResource(R.string.filter_all) to null) + options).forEach { (text, value) ->
                DropdownMenuItem(
                    text = { Text(text = text, style = FalesiaTheme.typography.chip) },
                    onClick = {
                        expanded = false
                        onSelect(value)
                    },
                )
            }
        }
    }
}

@Composable
private fun RouteListContent(
    content: RouteListUiState.Content,
    onIntent: (RouteListIntent) -> Unit,
    innerPadding: PaddingValues,
) {
    val spacing = FalesiaTheme.spacing
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + spacing.space2,
            bottom = innerPadding.calculateBottomPadding() + spacing.space6,
            start = spacing.space4,
            end = spacing.space4,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        when {
            content.routesLoading -> item(key = "loading") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(spacing.space6),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            content.routesNotDownloaded -> item(key = "not_downloaded") {
                Message(stringResource(R.string.routes_not_downloaded))
            }

            content.routesError -> item(key = "error") {
                RoutesError(onRetry = {
                    onIntent(
                        RouteListIntent.Retry
                    )
                })
            }

            !content.hasRoutes -> item(key = "empty") { Message(stringResource(R.string.routes_empty)) }
            content.sections.isEmpty() -> item(key = "no_match") {
                NoMatch(onClear = {
                    onIntent(
                        RouteListIntent.ClearFilters
                    )
                })
            }

            else -> content.sections.forEach { section ->
                item(key = "settore_${section.settore.id}") {
                    SectionHeader(
                        title = section.settore.nome,
                        titleStyle = FalesiaTheme.typography.titleSm,
                        modifier = Modifier.padding(top = spacing.space3),
                    )
                }
                items(section.vie, key = { "via_${it.id}" }) { via ->
                    RouteListItem(
                        name = via.nome,
                        grade = via.grado.ifBlank { stringResource(R.string.value_unknown) },
                        gradeLabel = stringResource(R.string.label_grade),
                        length = via.length.ifBlank { null },
                        rating = via.rating?.let(::ratingText),
                        style = via.style.label(),
                        modifier = Modifier.testTag(TAG_ROUTE_ITEM),
                    )
                }
            }
        }
    }
}

@Composable
private fun NoMatch(onClear: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.route_list_no_match),
            style = FalesiaTheme.typography.bodySm,
            color = FalesiaTheme.colors.inkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = FalesiaTheme.spacing.space6),
        )
        TextButton(onClick = onClear) {
            Text(
                text = stringResource(R.string.clear_filters),
                style = FalesiaTheme.typography.link,
                color = FalesiaTheme.colors.primaryText,
            )
        }
    }
}

@Composable
private fun CenteredProgress(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun RouteListScreenPreview() {
    AppTheme {
        RouteListScreen(
            uiState = reduceRouteList(previewCrag, RouteFilters(), RoutesStatus.Idle),
            onIntent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun RouteListScreenNoMatchPreview() {
    AppTheme {
        RouteListScreen(
            uiState = reduceRouteList(
                previewCrag,
                RouteFilters(band = GradeBand.EightPlus, style = ViaStyle.Trad),
                RoutesStatus.Idle
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun RouteListScreenLargeFontPreview() {
    AppTheme {
        RouteListScreen(
            uiState = reduceRouteList(previewCrag, RouteFilters(query = "6"), RoutesStatus.Idle),
            onIntent = {},
        )
    }
}
