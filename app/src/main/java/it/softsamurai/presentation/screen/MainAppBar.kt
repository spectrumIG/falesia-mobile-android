@file:OptIn(ExperimentalMaterial3Api::class)

package it.softsamurai.presentation.screen

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import it.softsamurai.R
import it.softsamurai.core.ui.AppTypography
import it.softsamurai.core.ui.primaryContainerLight

@Composable
fun MainAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = { HomeMainTitle() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            .copy(containerColor = primaryContainerLight),
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    )
}

@Composable
internal fun HomeMainTitle(modifier: Modifier = Modifier) {
    Text(text = stringResource(R.string.home_title_lbl), style = AppTypography.headlineLarge)
}

@Preview
@Composable
private fun MainAppBarPreview() {
    MainAppBar()
}
