package it.softsamurai.presentation.navigation

import androidx.navigation3.runtime.NavKey

sealed interface AppRoute {
    data object Discover : AppRoute, NavKey
    data object Logbook : AppRoute
    data object Ticklist : AppRoute
    data object Profile : AppRoute
}
