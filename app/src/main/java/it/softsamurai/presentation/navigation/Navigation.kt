package it.softsamurai.presentation.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.presentation.screen.CragDetailsRoute
import it.softsamurai.presentation.screen.DiscoverRoute
import it.softsamurai.presentation.screen.LogbookScreen
import it.softsamurai.presentation.screen.ProfileScreen
import it.softsamurai.presentation.screen.RouteListRoute
import it.softsamurai.presentation.screen.TicklistScreen

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(AppRoute.Discover)
    Scaffold(
        containerColor = FalesiaTheme.colors.bg,
        bottomBar = {
            AppBottomBar(
                currentRoute = backStack.selectedTopLevel(),
                onNavigate = { route -> backStack.navigateToTopLevel(route) },
            )
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            // Screens draw their own top bar: hand them the insets the bottom bar did not use.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            onBack = { backStack.goBack() },
            // Each entry gets its own ViewModels: two crags on the back stack never share one.
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<AppRoute.Discover> {
                    DiscoverRoute(onCragClick = { id -> backStack.navigateTo(AppRoute.CragDetails(id)) })
                }
                entry<AppRoute.Logbook> { LogbookScreen() }
                entry<AppRoute.Ticklist> { TicklistScreen() }
                entry<AppRoute.Profile> { ProfileScreen() }
                entry<AppRoute.CragDetails> { key ->
                    CragDetailsRoute(
                        falesiaId = key.falesiaId,
                        onBack = { backStack.goBack() },
                        onSeeAllRoutes = { id -> backStack.navigateTo(AppRoute.RouteList(id)) },
                    )
                }
                entry<AppRoute.RouteList> { key ->
                    RouteListRoute(falesiaId = key.falesiaId, onBack = { backStack.goBack() })
                }
            },
        )
    }
}

@Composable
fun MainNavigation() = AppNavigation()
