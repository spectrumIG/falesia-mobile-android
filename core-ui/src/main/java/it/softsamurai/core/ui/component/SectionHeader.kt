package it.softsamurai.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme

/**
 * A section title with an optional text action on the right (design component `SectionHeader`).
 * [titleStyle] defaults to `section-title`, the first heading of a list screen.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = FalesiaTheme.typography.sectionTitle,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = titleStyle,
            color = FalesiaTheme.colors.ink,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        actionLabel?.let {
            Text(
                text = it,
                style = FalesiaTheme.typography.link,
                color = FalesiaTheme.colors.primaryText,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onActionClick)
                    .padding(vertical = FalesiaTheme.spacing.space2),
            )
        }
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun SectionHeaderPreview() {
    AppTheme {
        Surface {
            SectionHeader(
                title = "Nearby crags",
                actionLabel = "Map view",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun SectionHeaderLargeFontPreview() {
    AppTheme {
        SectionHeader(
            title = "Nearby crags",
            actionLabel = "Map view",
            modifier = Modifier.padding(16.dp)
        )
    }
}
