package it.softsamurai.presentation

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.presentation.screen.LogbookScreen
import it.softsamurai.presentation.screen.ProfileScreen
import it.softsamurai.presentation.screen.TAG_PLACEHOLDER_TITLE
import it.softsamurai.presentation.screen.TicklistScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

private const val CENTER_TOLERANCE_DP = 1f

class PlaceholderScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun logbook_showsItsName() {
        composeRule.setContent { AppTheme { LogbookScreen() } }

        composeRule.onNodeWithTag(TAG_PLACEHOLDER_TITLE)
            .assertTextEquals(context.getString(R.string.nav_logbook))
    }

    @Test
    fun ticklist_showsItsName_darkTheme() {
        composeRule.setContent { AppTheme(darkTheme = true) { TicklistScreen() } }

        composeRule.onNodeWithTag(TAG_PLACEHOLDER_TITLE)
            .assertTextEquals(context.getString(R.string.nav_ticklist))
    }

    @Test
    fun profile_showsItsName_lightTheme() {
        composeRule.setContent { AppTheme(darkTheme = false) { ProfileScreen() } }

        composeRule.onNodeWithTag(TAG_PLACEHOLDER_TITLE)
            .assertTextEquals(context.getString(R.string.nav_profile))
    }

    @Test
    fun title_isHorizontallyCentered() {
        composeRule.setContent { AppTheme { ProfileScreen() } }

        val title = composeRule.onNodeWithTag(TAG_PLACEHOLDER_TITLE).getUnclippedBoundsInRoot()
        val root = composeRule.onRoot().getUnclippedBoundsInRoot()

        val titleCenter = (title.left + title.right) / 2
        val rootCenter = (root.left + root.right) / 2
        assertTrue(
            "title center $titleCenter, screen center $rootCenter",
            abs(titleCenter.value - rootCenter.value) <= CENTER_TOLERANCE_DP,
        )
    }
}
