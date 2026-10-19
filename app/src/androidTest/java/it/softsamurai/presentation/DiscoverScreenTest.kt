package it.softsamurai.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import it.softsamurai.R
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via
import it.softsamurai.presentation.screen.DiscoverScreen
import it.softsamurai.presentation.screen.TAG_CRAG_CARD
import it.softsamurai.presentation.screen.TAG_CRAG_LOCATION
import it.softsamurai.presentation.screen.TAG_PULL_TO_REFRESH
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Locale

private const val SNACKBAR_TIMEOUT_MS = 5_000L

class DiscoverScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun string(@StringRes id: Int, vararg args: Any) = context.getString(id, *args)

    private fun setScreen(
        uiState: FalesiaItemUiState,
        darkTheme: Boolean = false,
        events: MutableSharedFlow<DiscoverEvent> = MutableSharedFlow(),
        onCragClick: (String) -> Unit = {},
    ) = composeRule.setContent {
        AppTheme(darkTheme = darkTheme) {
            DiscoverScreen(
                uiState = uiState,
                events = events,
                onCragClick = onCragClick
            )
        }
    }

    @Test
    fun successState_displaysListOfCrags_lightTheme() {
        setScreen(FalesiaItemUiState.Success(fakecrags), darkTheme = false)

        composeRule.onNodeWithText("Monte Arrubiu").assertIsDisplayed()
        composeRule.onNodeWithText("Finale Ligure").assertIsDisplayed()
    }

    @Test
    fun successState_displaysListOfCrags_darkTheme() {
        setScreen(FalesiaItemUiState.Success(fakecrags), darkTheme = true)

        composeRule.onNodeWithText("Monte Arrubiu").assertIsDisplayed()
        composeRule.onNodeWithText("Finale Ligure").assertIsDisplayed()
    }

    @Test
    fun header_showsTitleSearchFiltersAndSection() {
        setScreen(FalesiaItemUiState.Success(fakecrags))

        composeRule.onNodeWithText(string(R.string.crag_atlas)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.search_placeholder))
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.filter_style)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.nearby_crags)).assertIsDisplayed()
    }

    @Test
    fun cragCard_showsUppercaseStatLabels() {
        setScreen(FalesiaItemUiState.Success(fakecrags))

        val routesLabel = string(R.string.label_routes).uppercase(Locale.getDefault())
        composeRule.onAllNodesWithText(routesLabel).onFirst().assertIsDisplayed()
    }

    @Test
    fun clickOnCard_opensCragById() {
        var opened: String? = null
        setScreen(FalesiaItemUiState.Success(fakecrags), onCragClick = { opened = it })

        composeRule.onAllNodesWithTag(TAG_CRAG_CARD).onFirst().performClick()

        assertEquals("monte-arrubiu", opened)
    }

    @Test
    fun clickOnChevron_opensCragById() {
        var opened: String? = null
        setScreen(FalesiaItemUiState.Success(fakecrags), onCragClick = { opened = it })

        composeRule.onNodeWithContentDescription(string(R.string.cd_view_crag, "Monte Arrubiu"))
            .performClick()

        assertEquals("monte-arrubiu", opened)
    }

    @Test
    fun successState_pullToRefreshBoxIsPresent() {
        setScreen(FalesiaItemUiState.Success(fakecrags))

        composeRule.onNodeWithTag(TAG_PULL_TO_REFRESH).assertIsDisplayed()
    }

    @Test
    fun refreshingState_listStillVisible() {
        setScreen(FalesiaItemUiState.Success(fakecrags, isRefreshing = true))

        composeRule.onNodeWithText("Monte Arrubiu").assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_PULL_TO_REFRESH).assertIsDisplayed()
    }

    @Test
    fun cragCard_withZone_showsLocationLine() {
        setScreen(FalesiaItemUiState.Success(fakecrags.take(1)))

        composeRule.onNodeWithTag(TAG_CRAG_LOCATION, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Sardegna, Ogliastra", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun cragCard_withoutZone_hasNoLocationLine() {
        // The scraper stores an empty zone today: the card must not show an orphan location pin.
        setScreen(FalesiaItemUiState.Success(listOf(fakecrags.first().copy(zona = ""))))

        composeRule.onNodeWithText("Monte Arrubiu").assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_CRAG_LOCATION, useUnmergedTree = true).assertDoesNotExist()
    }

    /** Emits [event] once the screen collects events and checks that [messageRes] is shown. */
    private fun assertEventShowsSnackbar(event: DiscoverEvent, @StringRes messageRes: Int) {
        // A buffer slot so tryEmit never drops the event, as the ViewModel's buffered Channel does.
        val events = MutableSharedFlow<DiscoverEvent>(extraBufferCapacity = 1)
        setScreen(FalesiaItemUiState.Success(fakecrags), events = events)
        // Let LaunchedEffect subscribe before emitting.
        composeRule.waitForIdle()

        events.tryEmit(event)

        val message = string(messageRes)
        composeRule.waitUntil(SNACKBAR_TIMEOUT_MS) {
            composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun syncFailedEvent_showsErrorSnackbar() {
        assertEventShowsSnackbar(DiscoverEvent.SyncFailed, R.string.sync_error_snackbar)
    }

    @Test
    fun alreadyUpToDateEvent_showsUpToDateSnackbar_andKeepsList() {
        assertEventShowsSnackbar(DiscoverEvent.AlreadyUpToDate, R.string.refresh_up_to_date)

        composeRule.onNodeWithText("Monte Arrubiu").assertIsDisplayed()
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
