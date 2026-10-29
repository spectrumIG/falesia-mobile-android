package it.softsamurai.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation 3 destinations. Top-level destinations (bottom bar tabs) are objects; detail destinations are
 * data classes carrying the id of what they show, so the back stack is typed and saveable.
 */
@Serializable
sealed interface AppRoute : NavKey {

    /** The bottom bar tab that stays selected while this route is on screen. */
    val topLevel: AppRoute

    @Serializable
    data object Discover : AppRoute {
        override val topLevel: AppRoute get() = this
    }

    @Serializable
    data object Logbook : AppRoute {
        override val topLevel: AppRoute get() = this
    }

    @Serializable
    data object Ticklist : AppRoute {
        override val topLevel: AppRoute get() = this
    }

    @Serializable
    data object Profile : AppRoute {
        override val topLevel: AppRoute get() = this
    }

    /** Details of one crag, reached from Discover; [falesiaId] is `Falesia.id`. */
    @Serializable
    data class CragDetails(val falesiaId: String) : AppRoute {
        override val topLevel: AppRoute get() = Discover
    }

    /** All the routes of one crag, reached from its details ("See all"). */
    @Serializable
    data class RouteList(val falesiaId: String) : AppRoute {
        override val topLevel: AppRoute get() = Discover
    }

    /** One route with its sector's topo, reached from crag details or the route list; [viaId] is `Via.id`. */
    @Serializable
    data class RouteDetail(val falesiaId: String, val viaId: Long) : AppRoute {
        override val topLevel: AppRoute get() = Discover
    }
}
