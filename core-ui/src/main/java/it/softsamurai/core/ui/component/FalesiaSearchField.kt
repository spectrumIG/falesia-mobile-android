package it.softsamurai.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.R

private val FocusBorderWidth = 2.dp

/**
 * Large search field of Discover's header (design component `SearchField`): `size-field` tall, `field`
 * fill, `radius-xl`, 2dp accent border on focus, text in `input`.
 *
 * [placeholder] doubles as the accessible label, since the placeholder color is below 4.5:1.
 */
@Composable
fun FalesiaSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = FalesiaTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(FalesiaTheme.radius.xl)
    val spacing = FalesiaTheme.spacing

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(FalesiaTheme.size.field)
            .semantics { contentDescription = placeholder },
        singleLine = true,
        textStyle = FalesiaTheme.typography.input.copy(color = colors.ink),
        cursorBrush = SolidColor(colors.primaryText),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .background(colors.field, shape)
                    .border(
                        FocusBorderWidth,
                        if (isFocused) colors.primaryText else Color.Transparent,
                        shape
                    )
                    .padding(start = spacing.space4, end = spacing.space3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = colors.fieldIcon,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = spacing.space3),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = FalesiaTheme.typography.input,
                            color = colors.fieldPlaceholder,
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun FalesiaSearchFieldEmptyPreview() {
    AppTheme {
        Box(Modifier
            .background(FalesiaTheme.colors.bg)
            .padding(16.dp)) {
            FalesiaSearchField(
                value = "",
                onValueChange = {},
                placeholder = "Search climbing areas…"
            )
        }
    }
}

@Preview(name = "Filled", showBackground = true)
@Composable
private fun FalesiaSearchFieldFilledPreview() {
    AppTheme(darkTheme = false) {
        Box(Modifier.padding(16.dp)) {
            FalesiaSearchField(
                value = "Finale",
                onValueChange = {},
                placeholder = "Search climbing areas…"
            )
        }
    }
}
