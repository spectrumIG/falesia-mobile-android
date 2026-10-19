package it.softsamurai.presentation.navigation

import androidx.navigation3.runtime.NavKey

// Back stack rules, kept as pure functions over the mutable back stack so they can be unit-tested.
//
// Discover is the start destination and always the root: selecting another tab gives [Discover, tab],
// so system back from any tab returns to Discover before leaving the app.

/** Replaces the back stack with the one for the top-level [route]. */
internal fun MutableList<NavKey>.navigateToTopLevel(route: AppRoute) {
    val target = route.topLevel
    clear()
    add(AppRoute.Discover)
    if (target != AppRoute.Discover) add(target)
}

/** Pushes [route] on top of the current stack (detail screens). */
internal fun MutableList<NavKey>.navigateTo(route: AppRoute) {
    add(route)
}

/** Pops one entry; the root is never removed so the stack is never empty. */
internal fun MutableList<NavKey>.goBack() {
    if (size > 1) removeAt(lastIndex)
}

/** The tab to highlight for the current back stack. */
internal fun List<NavKey>.selectedTopLevel(): AppRoute? = (lastOrNull() as? AppRoute)?.topLevel
