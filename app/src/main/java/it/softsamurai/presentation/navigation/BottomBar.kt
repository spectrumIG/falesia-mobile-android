package it.softsamurai.presentation.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme

/**
 * Design component `BottomBar`: `bg` fill under a `primary-10` hairline, no selection pill; the active item
 * is in the accent with a filled icon and a 600 label, the others in `nav-inactive`.
 */
@Composable
internal fun AppBottomBar(
    currentRoute: AppRoute?,
    onNavigate: (AppRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FalesiaTheme.colors
    Column(modifier = modifier) {
        HorizontalDivider(thickness = FalesiaTheme.size.hairline, color = colors.primary10)
        NavigationBar(containerColor = colors.bg, tonalElevation = 0.dp) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                val label = stringResource(item.labelRes)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            painter = painterResource(if (selected) item.selectedIcon else item.icon),
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = FalesiaTheme.typography.caption,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.primaryText,
                        selectedTextColor = colors.primaryText,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = colors.navInactive,
                        unselectedTextColor = colors.navInactive,
                    ),
                )
            }
        }
    }
}

// ──────────────────────── Previews ────────────────────────

@Preview(name = "BottomBar — light", showBackground = true)
@Preview(name = "BottomBar — dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppBottomBarPreview() {
    AppTheme {
        AppBottomBar(currentRoute = AppRoute.Discover, onNavigate = {})
    }
}

@Preview(name = "BottomBar — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun AppBottomBarLargeFontPreview() {
    AppTheme {
        AppBottomBar(currentRoute = AppRoute.Logbook, onNavigate = {})
    }
}
