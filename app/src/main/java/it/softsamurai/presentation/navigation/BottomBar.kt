package it.softsamurai.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import it.softsamurai.R

internal data class BottomNavItem(
    val route: AppRoute,
    @param:DrawableRes val icon: Int,
    val label: String,
)

internal val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(AppRoute.Discover, R.drawable.ic_explore, "Discover"),
    BottomNavItem(AppRoute.Logbook, R.drawable.ic_menu_book, "Logbook"),
    BottomNavItem(AppRoute.Ticklist, R.drawable.ic_check_circle, "Ticklist"),
    BottomNavItem(AppRoute.Profile, R.drawable.ic_person, "Profile"),
)

@Composable
internal fun AppBottomBar(
    currentRoute: AppRoute?,
    onNavigate: (AppRoute) -> Unit,
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(painterResource(item.icon), contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
