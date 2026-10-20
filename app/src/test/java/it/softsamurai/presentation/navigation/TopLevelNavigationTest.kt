package it.softsamurai.presentation.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TopLevelNavigationTest {

    private fun stackOf(vararg routes: AppRoute): MutableList<NavKey> = mutableListOf(*routes)

    // ── navigateToTopLevel ──

    @Test
    fun `selecting a tab puts it above Discover`() {
        val backStack = stackOf(AppRoute.Discover)

        backStack.navigateToTopLevel(AppRoute.Logbook)

        assertEquals(listOf(AppRoute.Discover, AppRoute.Logbook), backStack)
    }

    @Test
    fun `selecting a tab from another tab replaces it`() {
        val backStack = stackOf(AppRoute.Discover, AppRoute.Logbook)

        backStack.navigateToTopLevel(AppRoute.Profile)

        assertEquals(listOf(AppRoute.Discover, AppRoute.Profile), backStack)
    }

    @Test
    fun `selecting Discover leaves only Discover`() {
        val backStack = stackOf(AppRoute.Discover, AppRoute.Ticklist)

        backStack.navigateToTopLevel(AppRoute.Discover)

        assertEquals(listOf(AppRoute.Discover), backStack)
    }

    @Test
    fun `selecting a tab from a detail screen drops the detail`() {
        val backStack = stackOf(AppRoute.Discover, AppRoute.CragDetails("arco"))

        backStack.navigateToTopLevel(AppRoute.Ticklist)

        assertEquals(listOf(AppRoute.Discover, AppRoute.Ticklist), backStack)
    }

    @Test
    fun `navigating to top level with a detail route opens its tab`() {
        val backStack = stackOf(AppRoute.Discover, AppRoute.Profile)

        backStack.navigateToTopLevel(AppRoute.CragDetails("arco"))

        assertEquals(listOf(AppRoute.Discover), backStack)
    }

    // ── navigateTo / goBack ──

    @Test
    fun `navigateTo pushes a detail route with its id`() {
        val backStack = stackOf(AppRoute.Discover)

        backStack.navigateTo(AppRoute.CragDetails("arco"))

        assertEquals(listOf(AppRoute.Discover, AppRoute.CragDetails("arco")), backStack)
    }

    @Test
    fun `route list opens above the crag details and back returns to them`() {
        val backStack = stackOf(AppRoute.Discover, AppRoute.CragDetails("arco"))

        backStack.navigateTo(AppRoute.RouteList("arco"))
        assertEquals(AppRoute.Discover, backStack.selectedTopLevel())
        backStack.goBack()

        assertEquals(listOf(AppRoute.Discover, AppRoute.CragDetails("arco")), backStack)
    }

    @Test
    fun `back from a tab returns to Discover`() {
        val backStack = stackOf(AppRoute.Discover, AppRoute.Logbook)

        backStack.goBack()

        assertEquals(listOf(AppRoute.Discover), backStack)
    }

    @Test
    fun `back on the root keeps the stack`() {
        val backStack = stackOf(AppRoute.Discover)

        backStack.goBack()

        assertEquals(listOf(AppRoute.Discover), backStack)
    }

    // ── selectedTopLevel ──

    @Test
    fun `selected tab is the top level of the last route`() {
        assertEquals(
            AppRoute.Logbook,
            stackOf(AppRoute.Discover, AppRoute.Logbook).selectedTopLevel()
        )
        assertEquals(
            AppRoute.Discover,
            stackOf(AppRoute.Discover, AppRoute.CragDetails("arco")).selectedTopLevel(),
        )
    }

    @Test
    fun `no tab is selected on an empty stack`() {
        assertNull(stackOf().selectedTopLevel())
    }
}
