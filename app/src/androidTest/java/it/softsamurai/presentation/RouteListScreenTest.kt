package it.softsamurai.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.domain.model.GradeBand
import it.softsamurai.presentation.screen.RouteListScreen
import it.softsamurai.presentation.screen.TAG_FILTER_GRADE
import it.softsamurai.presentation.screen.TAG_ROUTE_ITEM
import it.softsamurai.presentation.screen.previewCrag
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RouteListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val intents = mutableListOf<RouteListIntent>()

    private fun string(@StringRes id: Int) = context.getString(id)

    private fun setScreen(filters: RouteFilters = RouteFilters(), darkTheme: Boolean = false) =
        composeRule.setContent {
            AppTheme(darkTheme = darkTheme) {
                RouteListScreen(
                    uiState = reduceRouteList(previewCrag, filters, RoutesStatus.Idle),
                    onIntent = { intents += it },
                )
            }
        }

    @Test
    fun routes_areGroupedBySector_lightTheme() {
        setScreen(darkTheme = false)

        composeRule.onNodeWithText("Pilastro Rosso").assertIsDisplayed()
        composeRule.onNodeWithText("il Paretone (sinistra)").assertIsDisplayed()
        assertEquals(3, composeRule.onAllNodesWithTag(TAG_ROUTE_ITEM).fetchSemanticsNodes().size)
    }

    @Test
    fun typingInSearch_sendsQuery_darkTheme() {
        setScreen(darkTheme = true)

        composeRule.onNode(hasSetTextAction()).performTextInput("zup")

        assertEquals(listOf<RouteListIntent>(RouteListIntent.QueryChanged("zup")), intents)
    }

    @Test
    fun gradeMenu_selectsABand() {
        setScreen()

        composeRule.onNodeWithTag(TAG_FILTER_GRADE).performClick()
        composeRule.onNodeWithText(string(R.string.grade_band_7)).performClick()

        assertEquals(
            listOf<RouteListIntent>(RouteListIntent.BandSelected(GradeBand.Seven)),
            intents
        )
    }

    @Test
    fun noMatch_offersToClearTheFilters() {
        setScreen(filters = RouteFilters(band = GradeBand.EightPlus))

        composeRule.onNodeWithText(string(R.string.route_list_no_match)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.clear_filters)).performClick()

        assertEquals(listOf<RouteListIntent>(RouteListIntent.ClearFilters), intents)
    }
}
