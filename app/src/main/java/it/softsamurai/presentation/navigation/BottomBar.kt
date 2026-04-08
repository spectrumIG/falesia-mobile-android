package it.softsamurai.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import it.softsamurai.R

internal data class BottomNavItem(
    val route: AppRoute,
    @param:DrawableRes val icon: Int,
    @param:StringRes val labelRes: Int,
)

internal val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(AppRoute.Discover, R.drawable.ic_explore, R.string.nav_discover),
    BottomNavItem(AppRoute.Logbook, R.drawable.ic_menu_book, R.string.nav_logbook),
    BottomNavItem(AppRoute.Ticklist, R.drawable.ic_check_circle, R.string.nav_ticklist),
    BottomNavItem(AppRoute.Profile, R.drawable.ic_person, R.string.nav_profile),
)

@Composable
internal fun AppBottomBar(
    currentRoute: AppRoute?,
    onNavigate: (AppRoute) -> Unit,
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(painterResource(item.icon), contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}
