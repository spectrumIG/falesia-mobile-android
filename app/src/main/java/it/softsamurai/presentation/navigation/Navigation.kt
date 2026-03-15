package it.softsamurai.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import it.softsamurai.presentation.screen.DiscoverRoute

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(AppRoute.Discover)

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = backStack.lastOrNull() as? AppRoute,
                onNavigate = { route ->
                    backStack.clear()
                    backStack.add(route)
                },
            )
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    is AppRoute.Discover -> NavEntry(key) { DiscoverRoute() }
                    is AppRoute.Logbook -> NavEntry(key) { PlaceholderScreen("Logbook") }
                    is AppRoute.Ticklist -> NavEntry(key) { PlaceholderScreen("Ticklist") }
                    is AppRoute.Profile -> NavEntry(key) { PlaceholderScreen("Profile") }
                    else -> error("Unknown route: $key")
                }
            },
        )
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun MainNavigation() = AppNavigation()
