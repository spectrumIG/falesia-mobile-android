package it.softsamurai.presentation.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import it.softsamurai.presentation.FalesiaItemUiState
import it.softsamurai.presentation.screen.DiscoverScreen
import it.softsamurai.presentation.screen.previewCrags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class DiscoverScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cragList_light() = composeRule.captureScreen {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }

    @Test
    fun cragList_dark() = composeRule.captureScreen(darkTheme = true) {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags))
    }

    /** First sync failed with an empty DB: error state with Retry instead of the endless spinner. */
    @Test
    fun firstSyncFailed() = composeRule.captureScreen {
        DiscoverScreen(uiState = FalesiaItemUiState.SyncFailed)
    }

    /** Debug build: download button on the right of the top bar, no pull-to-refresh. */
    @Test
    fun manualDownloads_showsDownloadButton() = composeRule.captureScreen {
        DiscoverScreen(uiState = FalesiaItemUiState.Success(previewCrags), manualDownloads = true)
    }
}
