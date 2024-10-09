package it.softsamurai.presentation.screen

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.softsamurai.R.drawable
import it.softsamurai.core.ui.AppTypography
import it.softsamurai.core.ui.onSurfaceVariantDarkMediumContrast
import it.softsamurai.core.ui.primaryLight
import it.softsamurai.repository.model.Falesia


@Composable
fun FalesiaItemUi(
    modifier: Modifier = Modifier,
    falesiaValue: Falesia
) {
    Card(
        modifier = Modifier.then(modifier).fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors()
            .copy(contentColor = onSurfaceVariantDarkMediumContrast)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Crop,
                painter = painterResource(drawable.montagna),
                contentDescription = ""
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = falesiaValue.nome,
                    fontStyle = AppTypography.headlineMedium.fontStyle,
                    fontSize = AppTypography.headlineMedium.fontSize,
                    color = primaryLight
                )
                Text(
                    text = falesiaValue.numeroVie.toString(),
                    fontStyle = AppTypography.titleMedium.fontStyle,
                    fontSize = AppTypography.titleMedium.fontSize,
                    color = primaryLight
                )
                Text(
                    text = falesiaValue.linkDettagli,
                    fontStyle = AppTypography.titleMedium.fontStyle,
                    fontSize = AppTypography.titleMedium.fontSize,
                    color = primaryLight
                )
            }


        }
    }
}


@Preview(showBackground = true)
@Composable
private fun FalesiaItemUiPreview() {
    FalesiaItemUi(
        falesiaValue = Falesia(
            nome = "delicata",
            zona = "lacus",
            numeroVie = 3671,
            linkDettagli = "ludus"
        ),
    )

}
