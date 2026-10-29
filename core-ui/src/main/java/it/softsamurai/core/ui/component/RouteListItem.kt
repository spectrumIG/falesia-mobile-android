package it.softsamurai.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.PillShape
import it.softsamurai.core.ui.R
import java.util.Locale

private val MetaIconSize = 18.dp
private val TagHorizontalPadding = 8.dp
private val TagVerticalPadding = 2.dp
private val CardElevation = 1.dp
private val NumberSize = 28.dp
private val SelectedBorder = 2.dp

/**
 * A route in the route list (design component `RouteListItem`): name, then length, rating and style tag,
 * and the grade (`grade-xl`) with its [gradeLabel] caption on the right. Null values are left out.
 *
 * On the route detail the routes of a sector carry their [number] on the topo, and the [selected] one has a
 * `primary` border. With [onClick] the item opens (or selects) the route.
 */
@Composable
fun RouteListItem(
    name: String,
    grade: String,
    gradeLabel: String,
    modifier: Modifier = Modifier,
    length: String? = null,
    rating: String? = null,
    style: String? = null,
    number: String? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = FalesiaTheme.colors
    val shape = RoundedCornerShape(FalesiaTheme.radius.xl)
    val border = if (selected) {
        BorderStroke(SelectedBorder, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(FalesiaTheme.size.hairline, colors.primary10)
    }
    val itemModifier = modifier
        .fillMaxWidth()
        .semantics(mergeDescendants = true) {
            this.selected = selected
            if (onClick != null) role = Role.Button
        }
    val content: @Composable () -> Unit = {
        RouteListItemContent(name, grade, gradeLabel, length, rating, style, number, selected)
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = itemModifier,
            shape = shape,
            color = colors.surfaceCardNeutral,
            border = border,
            shadowElevation = CardElevation,
            content = content,
        )
    } else {
        Surface(
            modifier = itemModifier,
            shape = shape,
            color = colors.surfaceCardNeutral,
            border = border,
            shadowElevation = CardElevation,
            content = content,
        )
    }
}

@Composable
private fun RouteListItemContent(
    name: String,
    grade: String,
    gradeLabel: String,
    length: String?,
    rating: String?,
    style: String?,
    number: String?,
    selected: Boolean,
) {
    val colors = FalesiaTheme.colors
    val spacing = FalesiaTheme.spacing
    Row(
        modifier = Modifier.padding(spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        number?.let { RouteNumber(it, selected) }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.space1),
        ) {
            Text(
                text = name,
                style = FalesiaTheme.typography.titleSm,
                color = colors.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space3),
            ) {
                length?.let { MetaItem(R.drawable.ic_straighten, it) }
                rating?.let { MetaItem(R.drawable.ic_star_filled, it) }
                style?.let {
                    Text(
                        text = it.uppercase(Locale.getDefault()),
                        style = FalesiaTheme.typography.tag,
                        color = colors.primaryText,
                        modifier = Modifier
                            .background(
                                colors.primary10,
                                RoundedCornerShape(FalesiaTheme.radius.default)
                            )
                            .padding(
                                horizontal = TagHorizontalPadding,
                                vertical = TagVerticalPadding
                            ),
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = grade,
                style = FalesiaTheme.typography.gradeXl,
                color = colors.primaryText
            )
            Text(
                text = gradeLabel.uppercase(Locale.getDefault()),
                style = FalesiaTheme.typography.micro,
                color = colors.inkFaint,
            )
        }
    }
}

/** The route's number on the topo: `primary` when it is the selected route, as the badge drawn on the photo. */
@Composable
private fun RouteNumber(number: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = NumberSize, minHeight = NumberSize)
            .background(
                if (selected) MaterialTheme.colorScheme.primary else FalesiaTheme.colors.primary10,
                PillShape,
            )
            .padding(horizontal = FalesiaTheme.spacing.space1),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number,
            style = FalesiaTheme.typography.label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else FalesiaTheme.colors.primaryText,
        )
    }
}

@Composable
private fun MetaItem(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space1),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = FalesiaTheme.colors.primaryText,
            modifier = Modifier.size(MetaIconSize),
        )
        Text(
            text = text,
            style = FalesiaTheme.typography.bodySm,
            color = FalesiaTheme.colors.inkMuted
        )
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun RouteListItemPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(FalesiaTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RouteListItem(
                name = "Sanguisughe",
                grade = "6c",
                gradeLabel = "Grade",
                length = "15–20m",
                rating = "3.0",
                style = "Sport"
            )
            RouteListItem(name = "Senza dati", grade = "?", gradeLabel = "Grade")
        }
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun RouteListItemLargeFontPreview() {
    AppTheme {
        RouteListItem(
            name = "Ci penserò domani",
            grade = "5c+",
            gradeLabel = "Grade",
            length = "18–22m",
            rating = "3.0",
            style = "Sport"
        )
    }
}
