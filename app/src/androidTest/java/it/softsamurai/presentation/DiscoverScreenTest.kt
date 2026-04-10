package it.softsamurai.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via
import it.softsamurai.presentation.screen.DiscoverScreen
import it.softsamurai.presentation.screen.TAG_PULL_TO_REFRESH
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class DiscoverScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun successState_displaysListOfCrags() {
        composeRule.setContent {
            AppTheme {
                DiscoverScreen(uiState = FalesiaItemUiState.Success(fakecrags))
            }
        }

        composeRule.onNodeWithText("Monte Arrubiu").assertIsDisplayed()
        composeRule.onNodeWithText("Finale Ligure").assertIsDisplayed()
    }

    @Test
    fun successState_pullToRefreshBoxIsPresent() {
        composeRule.setContent {
            AppTheme {
                DiscoverScreen(uiState = FalesiaItemUiState.Success(fakecrags))
            }
        }

        composeRule.onNodeWithTag(TAG_PULL_TO_REFRESH).assertIsDisplayed()
    }

    @Test
    fun refreshingState_listStillVisible() {
        composeRule.setContent {
            AppTheme {
                DiscoverScreen(
                    uiState = FalesiaItemUiState.Success(fakecrags, isRefreshing = true),
                )
            }
        }

        composeRule.onNodeWithText("Monte Arrubiu").assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_PULL_TO_REFRESH).assertIsDisplayed()
    }

    @Test
    fun syncErrorEvent_showsSnackbar() {
        val errorFlow = MutableSharedFlow<Unit>()

        composeRule.setContent {
            AppTheme {
                DiscoverScreen(
                    uiState = FalesiaItemUiState.Success(fakecrags),
                    syncErrorEvent = errorFlow,
                )
            }
        }

        composeRule.runOnUiThread { errorFlow.tryEmit(Unit) }
        composeRule.onNodeWithText("Couldn't refresh. Showing cached data.").assertIsDisplayed()
    }

    private val fakecrags = listOf(
        Falesia(
            id = "monte-arrubiu",
            nome = "Monte Arrubiu",
            zona = "Sardegna, Ogliastra",
            pictureUrl = null,
            latitude = 39.5,
            longitude = 9.5,
            numeroVie = 42,
            linkDettagli = "https://example.com/monte-arrubiu",
            vie = listOf(Via(nome = "Via del Sole", grado = "6a")),
            lastUpdated = 0L,
        ),
        Falesia(
            id = "finale-ligure",
            nome = "Finale Ligure",
            zona = "Liguria",
            pictureUrl = null,
            latitude = 44.16,
            longitude = 8.34,
            numeroVie = 128,
            linkDettagli = "https://example.com/finale",
            vie = listOf(Via(nome = "Spacca Tutto", grado = "8a")),
            lastUpdated = 0L,
        ),
    )
}
