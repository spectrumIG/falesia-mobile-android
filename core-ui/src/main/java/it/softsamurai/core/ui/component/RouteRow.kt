package it.softsamurai.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme

/**
 * A featured route on crag details (design component `RouteRow`): grade box, name, stars with the number of
 * votes, type and length. [rating] null hides the stars (route without votes); [ratingDescription] is read
 * by screen readers instead of the stars.
 */
@Composable
fun RouteRow(
    grade: String,
    name: String,
    modifier: Modifier = Modifier,
    rating: Double? = null,
    ratingCount: Int = 0,
    ratingDescription: String = "",
    type: String? = null,
    length: String? = null,
) {
    val colors = FalesiaTheme.colors
    val spacing = FalesiaTheme.spacing
    val shape = RoundedCornerShape(FalesiaTheme.radius.xl)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .background(colors.surfaceRow, shape)
            .border(FalesiaTheme.size.hairline, colors.primary10, shape)
            .padding(spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        Box(
            modifier = Modifier
                .size(FalesiaTheme.size.gradeBox)
                .background(colors.primary20, RoundedCornerShape(FalesiaTheme.radius.lg)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = grade,
                style = FalesiaTheme.typography.stat,
                color = colors.primaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = FalesiaTheme.typography.itemTitle,
                color = colors.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            rating?.let {
                StarRating(
                    rating = it,
                    ratingCount = ratingCount,
                    contentDescription = ratingDescription
                )
            }
        }
        if (type != null || length != null) {
            Column(horizontalAlignment = Alignment.End) {
                type?.let {
                    Text(
                        text = it,
                        style = FalesiaTheme.typography.caption,
                        color = colors.inkDim
                    )
                }
                length?.let {
                    Text(
                        text = it,
                        style = FalesiaTheme.typography.caption,
                        color = colors.inkFaint
                    )
                }
            }
        }
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun RouteRowPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(FalesiaTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RouteRow(
                grade = "7a+",
                name = "Zuppa inglese",
                rating = 4.5,
                ratingCount = 48,
                type = "Sport",
                length = "25m"
            )
            RouteRow(grade = "5c", name = "Senza voti", type = "Sport", length = "18–22m")
        }
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun RouteRowLargeFontPreview() {
    AppTheme {
        RouteRow(
            grade = "6b+",
            name = "Ditta Cambi & Corsi",
            rating = 3.0,
            ratingCount = 5,
            type = "Sport",
            length = "35m"
        )
    }
}
