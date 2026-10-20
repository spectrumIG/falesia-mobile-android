package it.softsamurai.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.platform.app.InstrumentationRegistry
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.presentation.screen.CragDetailsScreen
import it.softsamurai.presentation.screen.TAG_FEATURED_ROUTE
import it.softsamurai.presentation.screen.TAG_ROUTES_ERROR
import it.softsamurai.presentation.screen.previewCrag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class CragDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val intents = mutableListOf<CragDetailsIntent>()

    private fun string(@StringRes id: Int, vararg args: Any) = context.getString(id, *args)

    private fun setScreen(
        uiState: CragDetailsUiState = reduceCragDetails(previewCrag, RoutesStatus.Idle, false),
        darkTheme: Boolean = false,
        onBack: () -> Unit = {},
    ) = composeRule.setContent {
        AppTheme(darkTheme = darkTheme) {
            CragDetailsScreen(uiState = uiState, onIntent = { intents += it }, onBack = onBack)
        }
    }

    private fun scrollTo(text: String) {
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(text))
    }

    @Test
    fun content_showsHeroTilesAndDescription_lightTheme() {
        setScreen(darkTheme = false)

        composeRule.onNodeWithText(previewCrag.zona).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.info_approach)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.approach_minutes_range, 6, 21))
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.info_exposure)).assertIsDisplayed()
        composeRule.onNodeWithText("231").assertIsDisplayed()
        scrollTo(previewCrag.description)
        composeRule.onNodeWithText(previewCrag.description).assertIsDisplayed()
    }

    @Test
    fun content_showsFeaturedRoutesWithFrenchGrades_darkTheme() {
        setScreen(darkTheme = true)

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag(TAG_FEATURED_ROUTE))
        scrollTo("Ditta Cambi & Corsi")
        assertEquals(
            3,
            composeRule.onAllNodesWithTag(TAG_FEATURED_ROUTE).fetchSemanticsNodes().size
        )
        composeRule.onNodeWithText("7a+").assertIsDisplayed()
    }

    @Test
    fun seeAll_and_openNavigation_sendTheirIntents() {
        setScreen()

        val seeAll = string(R.string.see_all).uppercase(Locale.getDefault())
        scrollTo(seeAll)
        composeRule.onNodeWithText(seeAll).performClick()
        scrollTo(string(R.string.open_navigation))
        composeRule.onNodeWithText(string(R.string.open_navigation)).performClick()

        assertEquals(
            listOf(CragDetailsIntent.SeeAllRoutes, CragDetailsIntent.OpenNavigation),
            intents
        )
    }

    @Test
    fun routesError_showsRetry() {
        setScreen(
            uiState = reduceCragDetails(
                previewCrag.copy(vie = emptyList()),
                RoutesStatus.Failed,
                false
            )
        )

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag(TAG_ROUTES_ERROR))
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        assertEquals(listOf(CragDetailsIntent.Retry), intents)
    }

    @Test
    fun notFound_showsMessage_andBackWorks() {
        var back = false
        setScreen(uiState = CragDetailsUiState.NotFound, onBack = { back = true })

        composeRule.onNodeWithText(string(R.string.crag_not_found)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.cd_back)).performClick()
        assertTrue(back)
    }
}
