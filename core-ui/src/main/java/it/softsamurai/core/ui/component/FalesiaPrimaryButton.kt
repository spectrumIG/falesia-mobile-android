package it.softsamurai.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.R

private val ButtonElevation = 4.dp

/**
 * Full-width filled call to action (design component `Button`, primary): `primary` fill, `on-primary`
 * text in `button`, optional leading [icon], `size-button` tall.
 */
@Composable
fun FalesiaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(FalesiaTheme.size.button)
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(FalesiaTheme.radius.xl),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = if (enabled) ButtonElevation else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FalesiaTheme.spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                FalesiaTheme.spacing.space2,
                Alignment.CenterHorizontally
            ),
        ) {
            icon?.let { Icon(painter = painterResource(it), contentDescription = null) }
            Text(text = text, style = FalesiaTheme.typography.button)
        }
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun FalesiaPrimaryButtonPreview() {
    AppTheme {
        Surface {
            FalesiaPrimaryButton(
                text = "Open navigation",
                icon = R.drawable.ic_location_on,
                onClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
