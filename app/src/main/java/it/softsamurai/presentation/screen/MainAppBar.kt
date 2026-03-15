@file:OptIn(ExperimentalMaterial3Api::class)

package it.softsamurai.presentation.screen

import android.content.res.Configuration
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import it.softsamurai.R

@Composable
fun MainAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = { HomeMainTitle() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    )
}

@Composable
internal fun HomeMainTitle(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.home_title_lbl),
        style = MaterialTheme.typography.headlineLarge,
    )
}

// ──────────────────────── Previews ────────────────────────

@Preview(name = "AppBar — light", showBackground = true)
@Composable
private fun MainAppBarLightPreview() {
    MaterialTheme {
        MainAppBar()
    }
}

@Preview(
    name = "AppBar — dark theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MainAppBarDarkPreview() {
    MaterialTheme {
        MainAppBar()
    }
}

@Preview(name = "AppBar — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun MainAppBarLargeFontPreview() {
    MaterialTheme {
        MainAppBar()
    }
}

@Preview(name = "HomeMainTitle — standalone", showBackground = true)
@Composable
private fun HomeMainTitlePreview() {
    MaterialTheme {
        HomeMainTitle()
    }
}
