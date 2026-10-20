package it.softsamurai.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import it.softsamurai.R
import it.softsamurai.presentation.screen.TAG_PLACEHOLDER_TITLE
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun label(@StringRes id: Int) = composeTestRule.activity.getString(id)

    // Bottom bar items are the clickable nodes carrying the tab label.
    private fun tab(@StringRes id: Int) =
        composeTestRule.onNode(hasText(label(id)) and hasClickAction())

    @Test
    fun bottomBar_showsAllFourTabs() {
        listOf(
            R.string.nav_discover,
            R.string.nav_logbook,
            R.string.nav_ticklist,
            R.string.nav_profile
        )
            .forEach { tab(it).assertIsDisplayed() }
    }

    @Test
    fun bottomBar_discoverTabIsSelectedOnLaunch() {
        tab(R.string.nav_discover).assertIsSelected()
    }

    @Test
    fun bottomBar_clickLogbook_showsLogbookScreen() {
        tab(R.string.nav_logbook).performClick()

        composeTestRule.onNodeWithTag(TAG_PLACEHOLDER_TITLE)
            .assertTextEquals(label(R.string.nav_logbook))
        tab(R.string.nav_logbook).assertIsSelected()
        tab(R.string.nav_discover).assertIsNotSelected()
    }

    @Test
    fun bottomBar_clickTicklist_showsTicklistScreen() {
        tab(R.string.nav_ticklist).performClick()

        composeTestRule.onNodeWithTag(TAG_PLACEHOLDER_TITLE)
            .assertTextEquals(label(R.string.nav_ticklist))
        tab(R.string.nav_ticklist).assertIsSelected()
    }

    @Test
    fun bottomBar_clickProfile_showsProfileScreen() {
        tab(R.string.nav_profile).performClick()

        composeTestRule.onNodeWithTag(TAG_PLACEHOLDER_TITLE)
            .assertTextEquals(label(R.string.nav_profile))
        tab(R.string.nav_profile).assertIsSelected()
    }

    @Test
    fun bottomBar_navigateAwayAndBack_restoresDiscover() {
        tab(R.string.nav_logbook).performClick()
        tab(R.string.nav_logbook).assertIsSelected()

        tab(R.string.nav_discover).performClick()
        tab(R.string.nav_discover).assertIsSelected()
        tab(R.string.nav_logbook).assertIsNotSelected()
    }

    @Test
    fun systemBack_fromTab_returnsToDiscover() {
        tab(R.string.nav_profile).performClick()

        Espresso.pressBack()

        tab(R.string.nav_discover).assertIsSelected()
        tab(R.string.nav_profile).assertIsNotSelected()
    }
}
