package it.softsamurai.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.PillShape
import java.util.Locale

private val TagHorizontalPadding = 10.dp

/**
 * Climbing-style pill on crag cards (design component `Tag`, `fm-tag`): `primary-20` fill, accent text in
 * `tag`, uppercased here. Pass the text in normal case.
 */
@Composable
fun StyleTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(Locale.getDefault()),
        style = FalesiaTheme.typography.tag,
        color = FalesiaTheme.colors.primaryText,
        modifier = modifier
            .background(FalesiaTheme.colors.primary20, PillShape)
            .padding(horizontal = TagHorizontalPadding, vertical = FalesiaTheme.spacing.space1),
    )
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun StyleTagPreview() {
    AppTheme {
        Surface {
            StyleTag(text = "Sport", modifier = Modifier.padding(16.dp))
        }
    }
}
