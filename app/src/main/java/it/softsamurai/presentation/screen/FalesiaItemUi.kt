package it.softsamurai.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.R.drawable
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via

/** Compact crag row (thumbnail, name, routes count, source link) on the design-system tokens. */
@Composable
fun FalesiaItemUi(
    modifier: Modifier = Modifier,
    falesiaValue: Falesia,
) {
    val colors = FalesiaTheme.colors
    val spacing = FalesiaTheme.spacing
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FalesiaTheme.radius.xl),
        border = BorderStroke(FalesiaTheme.size.hairline, colors.line),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceCardNeutral,
            contentColor = colors.ink
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space4),
            horizontalArrangement = Arrangement.spacedBy(spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.size(FalesiaTheme.size.thumb),
                contentScale = ContentScale.Fit,
                painter = painterResource(drawable.montagna),
                contentDescription = null,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = falesiaValue.nome,
                    style = FalesiaTheme.typography.itemTitle,
                    color = colors.ink,
                )
                Text(
                    text = falesiaValue.numeroVie.toString(),
                    style = FalesiaTheme.typography.stat,
                    color = colors.primaryText,
                )
                Text(
                    text = falesiaValue.linkDettagli,
                    style = FalesiaTheme.typography.bodySm,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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

@PreviewLightDark
@Composable
private fun FalesiaItemUiPreview() {
    AppTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesia,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "No routes yet", showBackground = true)
@Composable
private fun FalesiaItemUiNoViePreview() {
    AppTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesiaNoVie,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Long name wrapping", showBackground = true)
@Composable
private fun FalesiaItemUiLongNamePreview() {
    AppTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesiaLongName,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(name = "Large font scale", showBackground = true, fontScale = 1.5f)
@Composable
private fun FalesiaItemUiLargeFontPreview() {
    AppTheme {
        FalesiaItemUi(
            falesiaValue = sampleFalesia,
            modifier = Modifier.padding(8.dp),
        )
    }
}
