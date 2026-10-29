package it.softsamurai.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.R
import java.util.Locale

private val MinValueSize = 12.sp

/**
 * A boxed quick fact, three across on crag details (design component `InfoTile`, `fm-tile`): `primary`
 * icon, label in `caption` `ink-muted`, value in `stat`, `primary-5` fill and `primary-20` border.
 */
@Composable
fun InfoTile(
    @DrawableRes icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(FalesiaTheme.radius.xl)
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .background(FalesiaTheme.colors.primary5, shape)
            .border(FalesiaTheme.size.hairline, FalesiaTheme.colors.primary20, shape)
            .padding(FalesiaTheme.spacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            FalesiaTheme.spacing.space2,
            Alignment.CenterVertically
        ),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = FalesiaTheme.typography.caption,
            color = FalesiaTheme.colors.inkMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        // Three tiles share a phone width: long values ("12–21 min") shrink to fit one line.
        Text(
            text = value,
            style = FalesiaTheme.typography.stat,
            color = FalesiaTheme.colors.ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = MinValueSize,
                maxFontSize = FalesiaTheme.typography.stat.fontSize,
            ),
        )
    }
}

/**
 * A quick fact aligned to the start, two across on the route detail (`fm-tile-start`): [label] uppercased in
 * the accent, value in `tile-value`. `primary-10` fill in dark, `primary-5` in light, `primary-20` border.
 */
@Composable
fun InfoTileStart(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = FalesiaTheme.colors
    val shape = RoundedCornerShape(FalesiaTheme.radius.xl)
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .background(if (colors.isDark) colors.primary10 else colors.primary5, shape)
            .border(FalesiaTheme.size.hairline, colors.primary20, shape)
            .padding(FalesiaTheme.spacing.space4),
        verticalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space1),
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = FalesiaTheme.typography.label,
            color = colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = FalesiaTheme.typography.tileValue,
            color = colors.ink,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = MinValueSize,
                maxFontSize = FalesiaTheme.typography.tileValue.fontSize,
            ),
        )
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun InfoTilePreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(FalesiaTheme.colors.bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoTile(
                icon = R.drawable.ic_location_on,
                label = "Approach",
                value = "20 min",
                modifier = Modifier.weight(1f)
            )
            InfoTile(
                icon = R.drawable.ic_star_filled,
                label = "Routes",
                value = "42",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun InfoTileStartPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(FalesiaTheme.colors.bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoTileStart(label = "Grade", value = "6a+", modifier = Modifier.weight(1f))
            InfoTileStart(label = "Length", value = "15–20m", modifier = Modifier.weight(1f))
        }
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun InfoTileLargeFontPreview() {
    AppTheme {
        InfoTile(
            icon = R.drawable.ic_location_on,
            label = "Exposure",
            value = "SW, S",
            modifier = Modifier.padding(16.dp)
        )
    }
}
