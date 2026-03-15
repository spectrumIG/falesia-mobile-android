package it.softsamurai.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.softsamurai.R.drawable
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via

@Composable
fun FalesiaItemUi(
    modifier: Modifier = Modifier,
    falesiaValue: Falesia,
) {
    Card(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.FillBounds,
                painter = painterResource(drawable.montagna),
                contentDescription = "",
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = falesiaValue.nome,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = falesiaValue.numeroVie.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = falesiaValue.linkDettagli,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// ──────────────────────── Previews ────────────────────────

private val sampleFalesia = Falesia(
    id = "monte-arrubiu",
    nome = "Monte Arrubiu",
    zona = "Sardegna, Ogliastra",
    pictureUrl = null,
    latitude = 39.5,
    longitude = 9.5,
    numeroVie = 42,
    linkDettagli = "https://falesiaonline.it/monte-arrubiu",
    vie = listOf(Via(nome = "Via del Sole", grado = "6a")),
    lastUpdated = 0L,
)

private val sampleFalesiaNoVie = Falesia(
    id = "grotta-del-corno",
    nome = "Grotta del Corno",
    zona = "Lazio",
    pictureUrl = null,
    latitude = 41.9,
    longitude = 13.1,
    numeroVie = 0,
    linkDettagli = "https://falesiaonline.it/grotta-del-corno",
    vie = emptyList(),
    lastUpdated = 0L,
)

private val sampleFalesiaLongName = Falesia(
    id = "rocca-di-santa-maria-delle-grazie",
    nome = "Rocca di Santa Maria delle Grazie al Monte Soratte",
    zona = "Lazio, Roma Nord",
    pictureUrl = null,
    latitude = 42.2,
    longitude = 12.5,
    numeroVie = 17,
    linkDettagli = "https://falesiaonline.it/rocca-santa-maria",
    vie = listOf(Via(nome = "Fessura Classica", grado = "5c")),
    lastUpdated = 0L,
)

@Preview(name = "Default crag", showBackground = true)
@Composable
private fun FalesiaItemUiPreview() {
    MaterialTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesia,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "No routes yet", showBackground = true)
@Composable
private fun FalesiaItemUiNoViePreview() {
    MaterialTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesiaNoVie,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Long name wrapping", showBackground = true)
@Composable
private fun FalesiaItemUiLongNamePreview() {
    MaterialTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesiaLongName,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(
    name = "Dark theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FalesiaItemUiDarkPreview() {
    MaterialTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesia,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Large font scale", showBackground = true, fontScale = 1.5f)
@Composable
private fun FalesiaItemUiLargeFontPreview() {
    MaterialTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesia,
            modifier = Modifier.padding(8.dp),
        )
    }
}
