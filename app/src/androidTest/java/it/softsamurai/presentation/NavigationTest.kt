package it.softsamurai.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomBar_showsAllFourTabs() {
        composeTestRule.onNodeWithText("Discover").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logbook").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ticklist").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun bottomBar_discoverTabIsSelectedOnLaunch() {
        composeTestRule.onNodeWithText("Discover").assertIsSelected()
    }

    @Test
    fun bottomBar_clickLogbook_showsLogbookScreen() {
        composeTestRule.onNodeWithText("Logbook").performClick()
        // The placeholder screen displays the tab label as its heading
        composeTestRule
            .onNodeWithText("Logbook", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Logbook").assertIsSelected()
        composeTestRule.onNodeWithText("Discover").assertIsNotSelected()
    }

    @Test
    fun bottomBar_clickTicklist_showsTicklistScreen() {
        composeTestRule.onNodeWithText("Ticklist").performClick()
        composeTestRule
            .onNodeWithText("Ticklist", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Ticklist").assertIsSelected()
    }

    @Test
    fun bottomBar_clickProfile_showsProfileScreen() {
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule
            .onNodeWithText("Profile", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsSelected()
    }

    @Test
    fun bottomBar_navigateAwayAndBack_restoresDiscover() {
        composeTestRule.onNodeWithText("Logbook").performClick()
        composeTestRule.onNodeWithText("Logbook").assertIsSelected()

        composeTestRule.onNodeWithText("Discover").performClick()
        composeTestRule.onNodeWithText("Discover").assertIsSelected()
        composeTestRule.onNodeWithText("Logbook").assertIsNotSelected()
    }
}
