package it.softsamurai.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.R

private const val MAX_STARS = 5
private const val HALF = 0.5
private val DefaultStarSize = 12.dp

/**
 * Five stars for a 0–5 [rating] in half steps (filled, half and empty), then the number of votes in
 * brackets when [ratingCount] is given (design component `RouteRow`). [contentDescription] replaces the
 * stars for accessibility, e.g. "Rated 4.5 out of 5 by 12 climbers".
 */
@Composable
fun StarRating(
    rating: Double,
    contentDescription: String,
    modifier: Modifier = Modifier,
    ratingCount: Int? = null,
    starSize: Dp = DefaultStarSize,
) {
    Row(
        modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space1),
    ) {
        repeat(MAX_STARS) { index ->
            val icon = when {
                rating >= index + 1 -> R.drawable.ic_star_filled
                rating >= index + HALF -> R.drawable.ic_star_half_filled
                else -> R.drawable.ic_star
            }
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = FalesiaTheme.colors.primaryText,
                modifier = Modifier.size(starSize),
            )
        }
        ratingCount?.let {
            Text(
                text = "($it)",
                style = FalesiaTheme.typography.caption,
                color = FalesiaTheme.colors.inkMuted,
                modifier = Modifier.padding(start = FalesiaTheme.spacing.space1),
            )
        }
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun StarRatingPreview() {
    AppTheme {
        Surface {
            StarRating(
                rating = 3.5,
                ratingCount = 48,
                contentDescription = "",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
