package it.softsamurai.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute: NavKey {
    @Serializable
    data object Discover : AppRoute

    @Serializable
    data object Logbook : AppRoute

    @Serializable
    data object Ticklist : AppRoute

    @Serializable
    data object Profile : AppRoute
}
