package it.softsamurai.presentation.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import it.softsamurai.core.ui.AppTheme

/**
 * Renders [content] in the app theme and compares it with the reference image of the running test
 * (`src/test/snapshots/<Class>.<method>.png`; `recordRoborazziDebug` writes it, `verifyRoborazziDebug` checks it).
 *
 * Runs in inspection mode, like a preview: Coil loads no images and the map skips MapLibre, whose native
 * library does not load on the JVM.
 */
fun ComposeContentTestRule.captureScreen(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    setContent {
        CompositionLocalProvider(LocalInspectionMode provides true) {
            AppTheme(darkTheme = darkTheme, content = content)
        }
    }
    onRoot().captureRoboImage()
}
