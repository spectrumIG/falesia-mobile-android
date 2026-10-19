package it.softsamurai.presentation.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.component.FalesiaTopAppBar
import it.softsamurai.core.ui.component.TopBarNavigation

/** App-name top bar for top-level screens, built on the design system's [FalesiaTopAppBar]. */
@Composable
fun MainAppBar(modifier: Modifier = Modifier) {
    FalesiaTopAppBar(
        title = stringResource(R.string.home_title_lbl),
        navigation = TopBarNavigation.Menu(stringResource(R.string.cd_menu)),
        modifier = modifier,
    )
}

@Composable
internal fun HomeMainTitle(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.home_title_lbl),
        style = FalesiaTheme.typography.screenTitle,
        color = FalesiaTheme.colors.ink,
        modifier = modifier,
    )
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun MainAppBarPreview() {
    AppTheme {
        MainAppBar()
    }
}

@Preview(name = "AppBar — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun MainAppBarLargeFontPreview() {
    AppTheme {
        MainAppBar()
    }
}

@Preview(name = "HomeMainTitle — standalone", showBackground = true)
@Composable
private fun HomeMainTitlePreview() {
    AppTheme {
        HomeMainTitle()
    }
}
