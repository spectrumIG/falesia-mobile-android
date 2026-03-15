package it.softsamurai.presentation.navigation

import it.softsamurai.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavigationConfigTest {

    // --- AppRoute ---

    @Test
    fun `AppRoute Discover exists`() {
        assertNotNull(AppRoute.Discover)
    }

    @Test
    fun `AppRoute Logbook exists`() {
        assertNotNull(AppRoute.Logbook)
    }

    @Test
    fun `AppRoute Ticklist exists`() {
        assertNotNull(AppRoute.Ticklist)
    }

    @Test
    fun `AppRoute Profile exists`() {
        assertNotNull(AppRoute.Profile)
    }

    // --- bottomNavItems configuration ---

    @Test
    fun `bottomNavItems has exactly four items`() {
        assertEquals(4, bottomNavItems.size)
    }

    @Test
    fun `bottomNavItems routes match all AppRoute destinations`() {
        val routes = bottomNavItems.map { it.route }
        assertEquals(AppRoute.Discover, routes[0])
        assertEquals(AppRoute.Logbook, routes[1])
        assertEquals(AppRoute.Ticklist, routes[2])
        assertEquals(AppRoute.Profile, routes[3])
    }

    @Test
    fun `bottomNavItems labels match expected tab names`() {
        val labels = bottomNavItems.map { it.label }
        assertEquals(listOf("Discover", "Logbook", "Ticklist", "Profile"), labels)
    }

    @Test
    fun `bottomNavItems icons are assigned correctly`() {
        assertEquals(R.drawable.ic_explore, bottomNavItems[0].icon)
        assertEquals(R.drawable.ic_menu_book, bottomNavItems[1].icon)
        assertEquals(R.drawable.ic_check_circle, bottomNavItems[2].icon)
        assertEquals(R.drawable.ic_person, bottomNavItems[3].icon)
    }

    @Test
    fun `bottomNavItems has no duplicate routes`() {
        val routes = bottomNavItems.map { it.route }
        assertEquals(routes.size, routes.distinct().size)
    }

    @Test
    fun `bottomNavItems has no duplicate labels`() {
        val labels = bottomNavItems.map { it.label }
        assertEquals(labels.size, labels.distinct().size)
    }
}
