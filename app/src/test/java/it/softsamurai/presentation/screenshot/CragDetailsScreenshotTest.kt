package it.softsamurai.presentation.screenshot

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.softsamurai.presentation.RoutesStatus
import it.softsamurai.presentation.reduceCragDetails
import it.softsamurai.presentation.screen.CragDetailsScreen
import it.softsamurai.presentation.screen.previewCrag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A tall screen (h1500dp): the whole page fits, from the hero to the featured routes. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h1500dp-xxhdpi")
class CragDetailsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val content = reduceCragDetails(previewCrag, RoutesStatus.Idle, isRefreshing = false)

    @Test
    fun content_light() = composeRule.captureScreen {
        CragDetailsScreen(uiState = content, onIntent = {})
    }

    @Test
    fun content_dark() = composeRule.captureScreen(darkTheme = true) {
        CragDetailsScreen(uiState = content, onIntent = {})
    }

    /** Debug build: routes never downloaded, the hint points to the download button in the top bar. */
    @Test
    fun manualDownloads_routesNotDownloaded() = composeRule.captureScreen {
        CragDetailsScreen(
            uiState = reduceCragDetails(
                previewCrag.copy(vie = emptyList(), vieUpdatedAt = null),
                RoutesStatus.Idle,
                isRefreshing = false,
                manualDownloads = true,
            ),
            onIntent = {},
            manualDownloads = true,
        )
    }
}
