package it.softsamurai.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.PillShape
import it.softsamurai.core.ui.R

/**
 * Round icon button (`fm-icon-btn`, `size-icon-button`). [filled] is the accent variant used by the crag
 * card chevron (`primary` fill, `on-primary` icon); otherwise the icon is drawn in the accent on no fill.
 */
@Composable
fun FalesiaIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(FalesiaTheme.size.iconButton)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        shape = PillShape,
        color = if (filled) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (filled) MaterialTheme.colorScheme.onPrimary else FalesiaTheme.colors.primaryText,
    ) {
        // Surface stretches its child to the button size: center the 24dp icon inside it.
        Box(contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(icon), contentDescription = null)
        }
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun FalesiaIconButtonPreview() {
    AppTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FalesiaIconButton(
                    icon = R.drawable.ic_menu,
                    contentDescription = "Menu",
                    onClick = {})
                FalesiaIconButton(
                    icon = R.drawable.ic_chevron_right,
                    contentDescription = "Open",
                    onClick = {},
                    filled = true,
                )
            }
        }
    }
}
