package it.softsamurai.presentation.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import it.softsamurai.domain.model.GradeBand
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.presentation.RouteFilters
import it.softsamurai.presentation.RoutesStatus
import it.softsamurai.presentation.reduceRouteList
import it.softsamurai.presentation.screen.RouteListScreen
import it.softsamurai.presentation.screen.previewCrag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class RouteListScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun routeList(filters: RouteFilters, darkTheme: Boolean = false) =
        composeRule.captureScreen(darkTheme) {
            RouteListScreen(uiState = reduceRouteList(previewCrag, filters, RoutesStatus.Idle), onIntent = {})
        }

    /** Routes grouped by sector, French grades, no filter selected. */
    @Test
    fun allRoutes_light() = routeList(RouteFilters())

    @Test
    fun allRoutes_dark() = routeList(RouteFilters(), darkTheme = true)

    /** Grade band 6 selected: one sector left, its chip highlighted. */
    @Test
    fun gradeBandFilter() = routeList(RouteFilters(band = GradeBand.Six))

    /** Filters that match nothing: empty state with "clear filters". */
    @Test
    fun noMatch() = routeList(RouteFilters(band = GradeBand.EightPlus, style = ViaStyle.Trad))
}
