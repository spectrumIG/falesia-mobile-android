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
import androidx.navigation3.runtime.entryProvider
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
            entryProvider = entryProvider { // Define your screen entries here
                entry<AppRoute.Discover> { // Entry for the Home screen
                    DiscoverRoute()
                }
                entry<AppRoute.Logbook> { // Entry for the Topics screen
                    PlaceholderScreen("Logbook")
                }

                entry<AppRoute.Ticklist> { key -> // The 'key' argument provides type-safe access to passed objects
                    PlaceholderScreen("Ticklist")
                }
                entry<AppRoute.Profile> { key -> // The 'key' argument provides type-safe access to passed objects
                    PlaceholderScreen("Ticklist")
                }

            }
        )
    }
}

@Composable
fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun MainNavigation() = AppNavigation()
