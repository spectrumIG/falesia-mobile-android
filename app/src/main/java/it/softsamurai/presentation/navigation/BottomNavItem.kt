package it.softsamurai.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import it.softsamurai.R

/** A bottom bar tab: its route, outlined/filled icons and label. */
internal data class BottomNavItem(
    val route: AppRoute,
    @param:DrawableRes val icon: Int,
    @param:DrawableRes val selectedIcon: Int,
    @param:StringRes val labelRes: Int,
)

internal val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(
        AppRoute.Discover,
        R.drawable.ic_explore,
        R.drawable.ic_explore_filled,
        R.string.nav_discover
    ),
    BottomNavItem(
        AppRoute.Logbook,
        R.drawable.ic_menu_book,
        R.drawable.ic_menu_book_filled,
        R.string.nav_logbook
    ),
    BottomNavItem(
        AppRoute.Ticklist,
        R.drawable.ic_check_circle,
        R.drawable.ic_check_circle_filled,
        R.string.nav_ticklist,
    ),
    BottomNavItem(
        AppRoute.Profile,
        R.drawable.ic_person,
        R.drawable.ic_person_filled,
        R.string.nav_profile
    ),
)
