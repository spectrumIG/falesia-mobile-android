package it.softsamurai.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import it.softsamurai.presentation.FalesiaItemUiState.Success
import it.softsamurai.presentation.FalesiaItemViewModel
import it.softsamurai.repository.model.Falesia

@Composable
fun FalesiaItemScreen(
    modifier: Modifier = Modifier,
    viewModel: FalesiaItemViewModel = hiltViewModel()
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()

    if (items is Success) {
        FalesiaItemScreen(
            items = (items as Success).data,
            modifier = modifier
        )
    }
}

@Composable
internal fun FalesiaItemScreen(
    items: List<Falesia>,
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
        items.forEach {
            Text("Saved item: $it")
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        FalesiaItemScreen(
            listOf(
                Falesia(
                    nome = "Puppa",
                    zona = "Puppa",
                    coordinate = 0.0 to 0.0,
                    numeroVie = 2,
                    linkDettagli = ""
                )
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun PortraitPreview() {
    AppTheme {
        FalesiaItemScreen(
            listOf(
                Falesia(
                    nome = "Puppa",
                    zona = "Puppa",
                    coordinate = 0.0 to 0.0,
                    numeroVie = 2,
                    linkDettagli = ""
                )
            )
        )

    }
}
