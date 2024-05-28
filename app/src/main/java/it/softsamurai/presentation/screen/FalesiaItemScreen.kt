package it.softsamurai.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import it.softsamurai.core.ui.MyApplicationTheme
import it.softsamurai.presentation.FalesiaItemUiState.Success
import it.softsamurai.presentation.FalesiaItemViewModel

@Composable
fun FalesiaItemScreen(modifier: Modifier = Modifier, viewModel: FalesiaItemViewModel = hiltViewModel()) {
    val items by  viewModel.uiState.collectAsStateWithLifecycle()

    if (items is Success) {
        FalesiaItemScreen(
            items = (items as Success).data,
            onSave = { name -> viewModel.addFalesiaItem(name) },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FalesiaItemScreen(
    items: List<String>,
    onSave: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        var nameFalesiaItem by remember { mutableStateOf("Compose") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = nameFalesiaItem,
                onValueChange = { nameFalesiaItem = it }
            )

            Button(modifier = Modifier.width(96.dp), onClick = { onSave(nameFalesiaItem) }) {
                Text("Save")
            }
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
    MyApplicationTheme {
        FalesiaItemScreen(listOf("Compose", "Room", "Kotlin"), onSave = {})
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun PortraitPreview() {
    MyApplicationTheme {
        FalesiaItemScreen(listOf("Compose", "Room", "Kotlin"), onSave = {})
    }
}
