@file:OptIn(ExperimentalMaterial3Api::class)

package it.softsamurai.presentation.screen

import android.annotation.SuppressLint
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.AppTypography
import it.softsamurai.core.ui.errorLight
import it.softsamurai.presentation.FalesiaItemUiState
import it.softsamurai.presentation.FalesiaItemViewModel
import it.softsamurai.repository.model.Falesia

@Composable
@SuppressLint("Suspicious indentation:")
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun FalesiaItemScreen(
    uiState: FalesiaItemUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(),
    ) { value ->

        when (uiState) {
            is FalesiaItemUiState.Error -> CreateErrorScreen(uiState)
            FalesiaItemUiState.Loading -> CreateLoadingScreen()
            is FalesiaItemUiState.Success -> {
                LazyColumn(modifier = Modifier.padding(value)) {
                    items(uiState.data) {
                       FalesiaItemUi(falesiaValue = it)
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
fun CreateErrorScreen(uiState: FalesiaItemUiState.Error) {

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
