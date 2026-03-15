@file:OptIn(ExperimentalMaterial3Api::class)

package it.softsamurai.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.softsamurai.R
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via
import it.softsamurai.presentation.FalesiaItemUiState
import it.softsamurai.presentation.FalesiaItemViewModel

@Composable
fun DiscoverRoute(
    viewModel: FalesiaItemViewModel = hiltViewModel<FalesiaItemViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DiscoverScreen(uiState = uiState)
}

@Composable
fun DiscoverScreen(uiState: FalesiaItemUiState) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Crag Atlas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = {
                        Text(
                            "Search climbing areas...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = null,
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                // Filter chips
                FilterChipsRow()
                Spacer(Modifier.height(4.dp))
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is FalesiaItemUiState.Loading -> LoadingContent(innerPadding)
            is FalesiaItemUiState.Error -> ErrorContent(uiState, innerPadding)
            is FalesiaItemUiState.Success -> CragListContent(uiState.data, innerPadding)
        }
    }
}

@Composable
private fun FilterChipsRow() {
    val filters = listOf("Style", "Grade Range", "Distance")
    var selected by remember { mutableStateOf(filters.first()) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(filters) { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { selected = filter },
                label = { Text(filter, style = MaterialTheme.typography.labelLarge) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_expand_more),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
private fun CragListContent(crags: List<Falesia>, innerPadding: PaddingValues) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = innerPadding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Nearby Crags",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = {}) {
                    Text(
                        "Map View",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        items(crags) { crag ->
            CragCard(crag = crag)
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
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(uiState: FalesiaItemUiState.Error, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = uiState.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun CragCard(crag: Falesia, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column {
            // Cover image
            AsyncImage(
                model = crag.pictureUrl,
                contentDescription = crag.nome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop,
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Name + location row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = crag.nome,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_location_on),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = crag.zona,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))

                // Stats + action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatColumn(
                        label = "ROUTES",
                        value = crag.numeroVie.toString(),
                    )
                    crag.vie.firstOrNull()?.let { via ->
                        Spacer(Modifier.width(24.dp))
                        StatColumn(
                            label = "BEST GRADE",
                            value = via.grado,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    FilledIconButton(
                        onClick = {},
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_forward),
                            contentDescription = "View ${crag.nome}",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

// Keep legacy composables — wired to DiscoverRoute
@Composable
fun FalesiaItemRoute(
    modifier: Modifier = Modifier,
    viewModel: FalesiaItemViewModel = hiltViewModel(),
) = DiscoverRoute(viewModel)

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

@Preview(name = "Loading — phone", showBackground = true, device = Devices.PHONE)
@Composable
private fun DiscoverScreenLoadingPreview() {
    MaterialTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Loading)
    }
}

@Preview(name = "Error — phone", showBackground = true, device = Devices.PHONE)
@Composable
private fun DiscoverScreenErrorPreview() {
    MaterialTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Error(message = "Network error: unable to reach server"))
    }
}

@Preview(name = "Success — phone", showBackground = true, device = Devices.PHONE)
@Composable
private fun DiscoverScreenSuccessPreview() {
    MaterialTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }
}

@Preview(
    name = "Success — dark theme",
    showBackground = true,
    device = Devices.PHONE,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DiscoverScreenDarkPreview() {
    MaterialTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }
}

@Preview(name = "Success — tablet", showBackground = true, device = Devices.TABLET)
@Composable
private fun DiscoverScreenTabletPreview() {
    MaterialTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }
}

@Preview(name = "Success — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun DiscoverScreenLargeFontPreview() {
    MaterialTheme {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }
}

@Preview(name = "CragCard — no vie", showBackground = true)
@Composable
private fun CragCardNoViePreview() {
    MaterialTheme {
        CragCard(
            crag = Falesia(
                id = "arco",
                nome = "Arco",
                zona = "Trentino-Alto Adige",
                pictureUrl = null,
                latitude = 45.92,
                longitude = 10.88,
                numeroVie = 0,
                linkDettagli = "https://example.com/arco",
                vie = emptyList(),
                lastUpdated = 0L,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "CragCard — with grade", showBackground = true)
@Composable
private fun CragCardWithGradePreview() {
    MaterialTheme {
        CragCard(
            crag = previewCrags.first(),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "CragCard — dark theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CragCardDarkPreview() {
    MaterialTheme {
        CragCard(
            crag = previewCrags.first(),
            modifier = Modifier.padding(16.dp),
        )
    }
}
