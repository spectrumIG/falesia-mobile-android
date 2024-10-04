package it.softsamurai.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object Home: Route()
}