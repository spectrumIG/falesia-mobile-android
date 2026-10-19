package it.softsamurai.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.R

private val ChipIconSize = 20.dp
private val SelectedChipElevation = 1.dp

/**
 * Discover filter chip (design component `FilterChip`): `size-chip` tall, `radius-xl`, `chip` fill and
 * `chip-ink` label; selected is the accent fill with `on-primary` label at weight 600. It opens a picker,
 * hence the trailing `expand_more`.
 */
@Composable
fun FalesiaFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FalesiaTheme.colors
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(FalesiaTheme.size.chip),
        shape = RoundedCornerShape(FalesiaTheme.radius.xl),
        color = if (selected) MaterialTheme.colorScheme.primary else colors.chip,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else colors.chipInk,
        shadowElevation = if (selected) SelectedChipElevation else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FalesiaTheme.spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space2),
        ) {
            Text(
                text = label,
                style = FalesiaTheme.typography.chip,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
            Icon(
                painter = painterResource(R.drawable.ic_expand_more),
                contentDescription = null,
                modifier = Modifier.size(ChipIconSize),
            )
        }
    }
}

/** Horizontal row of [FalesiaFilterChip]s: scrolls sideways, never wraps. */
@Composable
fun FalesiaFilterChipRow(
    labels: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = FalesiaTheme.spacing.space4),
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space3),
        contentPadding = contentPadding,
    ) {
        items(labels) { label ->
            FalesiaFilterChip(
                label = label,
                selected = label == selected,
                onClick = { onSelect(label) })
        }
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun FalesiaFilterChipRowPreview() {
    AppTheme {
        Surface {
            FalesiaFilterChipRow(
                labels = listOf("Style", "Grade range", "Distance"),
                selected = "Style",
                onSelect = {},
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}
