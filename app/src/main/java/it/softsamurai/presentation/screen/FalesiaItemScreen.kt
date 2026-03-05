@file:OptIn(ExperimentalMaterial3Api::class)

package it.softsamurai.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.AppTypography
import it.softsamurai.core.ui.errorLight
import it.softsamurai.presentation.FalesiaItemUiState
import it.softsamurai.presentation.FalesiaItemViewModel
import it.softsamurai.repository.model.Falesia

@Composable
@Suppress("Suspicious indentation:")
fun FalesiaItemRoute(
    modifier: Modifier = Modifier,
    viewModel: FalesiaItemViewModel = hiltViewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FalesiaItemScreen(uiState = uiState, modifier = modifier)
}

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun FalesiaItemScreen(
    uiState: FalesiaItemUiState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier =
        Modifier
            .fillMaxSize()
            .then(modifier),
        topBar = { MainAppBar() }
    ) { value ->

        when (uiState) {
            is FalesiaItemUiState.Error -> CreateErrorScreen(uiState)
            FalesiaItemUiState.Loading -> CreateLoadingScreen()
            is FalesiaItemUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.padding(value),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    items(uiState.data) {
                        FalesiaItemUi(modifier = Modifier.padding(vertical = 5.dp), falesiaValue = it)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateLoadingScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun CreateErrorScreen(uiState: FalesiaItemUiState.Error) {
    Text("ERRRORE", color = errorLight, fontSize = AppTypography.bodyLarge.fontSize)
}

// Previews

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        FalesiaItemScreen(
            uiState =
            FalesiaItemUiState.Success(
                listOf(
                    Falesia(
                        nome = "Puppa",
                        zona = "Puppa",
                        coordinate = 0.0 to 0.0,
                        numeroVie = 2,
                        linkDettagli = "",
                    ),
                ),
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun PortraitPreview() {
    AppTheme {
        FalesiaItemScreen(
            uiState =
            FalesiaItemUiState.Success(
                listOf(
                    Falesia(
                        nome = "Puppa",
                        zona = "Puppa",
                        coordinate = 0.0 to 0.0,
                        numeroVie = 2,
                        linkDettagli = "",
                    ),
                ),
            ),
        )
    }
}
