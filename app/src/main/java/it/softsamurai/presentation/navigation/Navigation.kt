package it.softsamurai.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import it.softsamurai.presentation.screen.DiscoverRoute

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(AppRoute.Discover)

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = backStack.lastOrNull(),
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
        ) { entry ->
            when (entry.key) {
                AppRoute.Discover -> DiscoverRoute()
                AppRoute.Logbook -> PlaceholderScreen("Logbook")
                AppRoute.Ticklist -> PlaceholderScreen("Ticklist")
                AppRoute.Profile -> PlaceholderScreen("Profile")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.headlineMedium)
    }
}

private data class BottomNavItem(
    val route: AppRoute,
    val icon: ImageVector,
    val label: String,
)

private val bottomNavItems = listOf(
    BottomNavItem(AppRoute.Discover, Icons.Default.Explore, "Discover"),
    BottomNavItem(AppRoute.Logbook, Icons.Default.MenuBook, "Logbook"),
    BottomNavItem(AppRoute.Ticklist, Icons.Default.CheckCircle, "Ticklist"),
    BottomNavItem(AppRoute.Profile, Icons.Default.Person, "Profile"),
)

@Composable
private fun AppBottomBar(
    currentRoute: Any?,
    onNavigate: (AppRoute) -> Unit,
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}

// Keep for backward compatibility — delegating to AppNavigation
@Composable
fun MainNavigation() = AppNavigation()

// Extension to clear and add in one operation
private fun NavBackStack.clear() {
    while (isNotEmpty()) removeLastOrNull()
}
