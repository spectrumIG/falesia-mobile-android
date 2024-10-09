package it.softsamurai.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    viewModel: FalesiaItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        FalesiaItemScreen(
            uiState = uiState,
            modifier = modifier
        )
}

@Composable
internal fun FalesiaItemScreen(
    uiState: FalesiaItemUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        var nameFalesiaItem by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        }
        when (uiState) {
            is FalesiaItemUiState.Error -> CreateErrorScreen()
            FalesiaItemUiState.Loading -> CreateLoadingScreen()
            is FalesiaItemUiState.Success -> {
                LazyColumn {
                    items(uiState.data) {
                        Text("Saved item: $it")
                    }

                }
            }
        }


    }
}

@Composable
fun CreateLoadingScreen() {
    CircularProgressIndicator()

}

@Composable
fun CreateErrorScreen() {

    Text("ERRRORE", color = errorLight, fontSize = AppTypography.bodyLarge.fontSize)
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        FalesiaItemScreen(
            uiState = FalesiaItemUiState.Success(
                listOf(
                Falesia(
                    nome = "Puppa",
                    zona = "Puppa",
                    coordinate = 0.0 to 0.0,
                    numeroVie = 2,
                    linkDettagli = ""
                )
                )
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun PortraitPreview() {
    AppTheme {
        FalesiaItemScreen(
            uiState = FalesiaItemUiState.Success(
                listOf(
                Falesia(
                    nome = "Puppa",
                    zona = "Puppa",
                    coordinate = 0.0 to 0.0,
                    numeroVie = 2,
                    linkDettagli = ""
                )
                )
            ),
        )

    }
}
