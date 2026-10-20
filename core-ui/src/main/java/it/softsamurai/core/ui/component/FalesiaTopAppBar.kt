package it.softsamurai.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.R
import java.util.Locale

private val ActionProgressSize = 20.dp
private val ActionProgressStroke = 2.dp

/**
 * Sticky bar at the top of every screen (design component `TopAppBar`).
 *
 * Without a [subtitle] the title is centered (`screen-title`); with one, title and uppercase subtitle
 * (`overline`, accent) are left aligned. [action] adds an icon button on the right. [content] is stacked below the title row on the same glass
 * background, before the `primary-10` hairline (Discover puts search and chips there).
 */
@Composable
fun FalesiaTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigation: TopBarNavigation? = null,
    subtitle: String? = null,
    action: TopBarAction? = null,
    containerColor: Color = FalesiaTheme.colors.bgGlass80,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val spacing = FalesiaTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            navigation?.let {
                FalesiaIconButton(
                    icon = it.icon,
                    contentDescription = it.contentDescription,
                    onClick = it.onClick,
                )
            }
            if (subtitle == null) {
                Text(
                    text = title,
                    style = FalesiaTheme.typography.screenTitle,
                    color = FalesiaTheme.colors.ink,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                )
                // Balances the leading button so the title stays centered on screen.
                if (action != null) {
                    TopBarActionButton(action)
                } else if (navigation != null) {
                    Spacer(Modifier.size(FalesiaTheme.size.iconButton))
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = FalesiaTheme.typography.screenTitle,
                        color = FalesiaTheme.colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = subtitle.uppercase(Locale.getDefault()),
                        style = FalesiaTheme.typography.overline,
                        color = FalesiaTheme.colors.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                action?.let { TopBarActionButton(it) }
            }
        }
        content()
        HorizontalDivider(
            thickness = FalesiaTheme.size.hairline,
            color = FalesiaTheme.colors.primary10
        )
    }
}

@Composable
private fun TopBarActionButton(action: TopBarAction) {
    if (action.isLoading) {
        Box(Modifier.size(FalesiaTheme.size.iconButton), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(ActionProgressSize),
                color = FalesiaTheme.colors.primaryText,
                strokeWidth = ActionProgressStroke,
            )
        }
    } else {
        FalesiaIconButton(icon = action.icon, contentDescription = action.contentDescription, onClick = action.onClick)
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun FalesiaTopAppBarCenteredPreview() {
    AppTheme {
        FalesiaTopAppBar(title = "Crag Atlas", navigation = TopBarNavigation.Menu("Menu"))
    }
}

@PreviewLightDark
@Composable
private fun FalesiaTopAppBarSubtitlePreview() {
    AppTheme {
        FalesiaTopAppBar(
            title = "Crag details",
            subtitle = "Sardegna, Ogliastra",
            navigation = TopBarNavigation.Back("Back") {},
        )
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun FalesiaTopAppBarLargeFontPreview() {
    AppTheme {
        FalesiaTopAppBar(title = "Crag Atlas", navigation = TopBarNavigation.Menu("Menu"))
    }
}
