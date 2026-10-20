package it.softsamurai.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.component.FalesiaTopAppBar
import it.softsamurai.core.ui.component.TopBarNavigation

const val TAG_PLACEHOLDER_TITLE = "placeholder_title"

/**
 * Empty page that only shows its name: stands in for sections not built yet. Top-level pages get the
 * `menu` button, detail pages pass a [TopBarNavigation.Back].
 */
@Composable
fun PlaceholderScreen(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigation: TopBarNavigation = TopBarNavigation.Menu(stringResource(R.string.cd_menu)),
) {
    val title = stringResource(titleRes)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalesiaTheme.colors.bg),
    ) {
        FalesiaTopAppBar(title = title, navigation = navigation, subtitle = subtitle)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(FalesiaTheme.spacing.space4),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space2),
            ) {
                Text(
                    text = title,
                    style = FalesiaTheme.typography.sectionTitle,
                    color = FalesiaTheme.colors.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag(TAG_PLACEHOLDER_TITLE),
                )
                Text(
                    text = stringResource(R.string.placeholder_coming_soon),
                    style = FalesiaTheme.typography.bodySm,
                    color = FalesiaTheme.colors.inkMuted,
                )
            }
        }
    }
}

@Composable
fun LogbookScreen(modifier: Modifier = Modifier) = PlaceholderScreen(R.string.nav_logbook, modifier)

@Composable
fun TicklistScreen(modifier: Modifier = Modifier) =
    PlaceholderScreen(R.string.nav_ticklist, modifier)

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) = PlaceholderScreen(R.string.nav_profile, modifier)

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun LogbookScreenPreview() {
    AppTheme { LogbookScreen() }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun ProfileScreenLargeFontPreview() {
    AppTheme { ProfileScreen() }
}
