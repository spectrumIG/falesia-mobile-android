package it.softsamurai.presentation.navigation

import it.softsamurai.R
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NavigationConfigTest {

    // --- AppRoute ---

    @Test
    fun `top-level routes are their own top level`() {
        listOf(AppRoute.Discover, AppRoute.Logbook, AppRoute.Ticklist, AppRoute.Profile).forEach {
            assertEquals(it, it.topLevel)
        }
    }

    @Test
    fun `CragDetails keeps Discover selected`() {
        assertEquals(AppRoute.Discover, AppRoute.CragDetails("arco").topLevel)
    }

    @Test
    fun `CragDetails equality is by falesiaId`() {
        assertEquals(AppRoute.CragDetails("arco"), AppRoute.CragDetails("arco"))
        assertNotEquals(AppRoute.CragDetails("arco"), AppRoute.CragDetails("finale-ligure"))
    }

    @Test
    fun `CragDetails survives serialization with its id`() {
        val route: AppRoute = AppRoute.CragDetails("monte-arrubiu")

        val decoded = Json.decodeFromString(
            AppRoute.serializer(),
            Json.encodeToString(AppRoute.serializer(), route)
        )

        assertEquals(route, decoded)
    }

    @Test
    fun `top-level routes survive serialization`() {
        bottomNavItems.map { it.route }.forEach { route ->
            val json = Json.encodeToString(AppRoute.serializer(), route)
            assertEquals(route, Json.decodeFromString(AppRoute.serializer(), json))
        }
    }

    // --- bottomNavItems configuration ---

    @Test
    fun `bottomNavItems has exactly four items`() {
        assertEquals(4, bottomNavItems.size)
    }

    @Test
    fun `bottomNavItems routes match all top-level destinations in order`() {
        assertEquals(
            listOf(AppRoute.Discover, AppRoute.Logbook, AppRoute.Ticklist, AppRoute.Profile),
            bottomNavItems.map { it.route },
        )
    }

    @Test
    fun `bottomNavItems labelRes match expected string resources`() {
        assertEquals(
            listOf(
                R.string.nav_discover,
                R.string.nav_logbook,
                R.string.nav_ticklist,
                R.string.nav_profile,
            ),
            bottomNavItems.map { it.labelRes },
        )
    }

    @Test
    fun `bottomNavItems icons are assigned correctly`() {
        assertEquals(
            listOf(
                R.drawable.ic_explore,
                R.drawable.ic_menu_book,
                R.drawable.ic_check_circle,
                R.drawable.ic_person
            ),
            bottomNavItems.map { it.icon },
        )
    }

    @Test
    fun `bottomNavItems selected icons are the filled variants`() {
        assertEquals(
            listOf(
                R.drawable.ic_explore_filled,
                R.drawable.ic_menu_book_filled,
                R.drawable.ic_check_circle_filled,
                R.drawable.ic_person_filled,
            ),
            bottomNavItems.map { it.selectedIcon },
        )
    }

    @Test
    fun `bottomNavItems has no duplicate routes`() {
        val routes = bottomNavItems.map { it.route }
        assertEquals(routes.size, routes.distinct().size)
    }

    @Test
    fun `bottomNavItems has no duplicate labelRes`() {
        val labelRes = bottomNavItems.map { it.labelRes }
        assertEquals(labelRes.size, labelRes.distinct().size)
    }
}
