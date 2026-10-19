package it.softsamurai.core.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext

// Material 3 schemes built from the design tokens. Translucent tokens (primary-5 cards, primary-10 lines …)
// are composited over `bg` so stock M3 components get solid colors.

private fun Color.over(background: Color) = compositeOver(background)

internal val darkScheme = with(darkFalesiaColors) {
    darkColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        primaryContainer = Primary,
        onPrimaryContainer = OnPrimary,
        inversePrimary = PrimaryTextLight,
        secondary = primaryText,
        onSecondary = OnPrimary,
        secondaryContainer = chip.over(bg),
        onSecondaryContainer = chipInk,
        tertiary = info,
        onTertiary = OnPrimary,
        background = bg,
        onBackground = ink,
        surface = bg,
        onSurface = ink,
        surfaceVariant = field.over(bg),
        onSurfaceVariant = inkMuted,
        surfaceTint = Primary,
        inverseSurface = ink,
        inverseOnSurface = bg,
        outline = inkMuted,
        outlineVariant = line.over(bg),
        surfaceDim = bg,
        surfaceBright = primary20.over(bg),
        surfaceContainerLowest = bg,
        surfaceContainerLow = primary5.over(bg),
        surfaceContainer = primary5.over(bg),
        surfaceContainerHigh = primary10.over(bg),
        surfaceContainerHighest = primary20.over(bg),
    )
}

internal val lightScheme = with(lightFalesiaColors) {
    lightColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        primaryContainer = Primary,
        onPrimaryContainer = OnPrimary,
        inversePrimary = Primary,
        secondary = primaryText,
        onSecondary = Color.White,
        secondaryContainer = chip,
        onSecondaryContainer = chipInk,
        tertiary = info,
        onTertiary = Color.White,
        background = bg,
        onBackground = ink,
        surface = bg,
        onSurface = ink,
        surfaceVariant = field.over(bg),
        onSurfaceVariant = inkMuted,
        surfaceTint = Primary,
        inverseSurface = BgDark,
        inverseOnSurface = darkFalesiaColors.ink,
        outline = inkMuted,
        outlineVariant = line,
        surfaceDim = line,
        surfaceBright = Color.White,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color.White,
        surfaceContainer = surfaceCard,
        surfaceContainerHigh = lineDivider,
        surfaceContainerHighest = line,
    )
}

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color,
)

/**
 * Access point to the design-system tokens that Material 3 has no slot for.
 * Use `MaterialTheme` for what it covers, `FalesiaTheme` for the rest.
 */
object FalesiaTheme {
    val colors: FalesiaColors
        @Composable @ReadOnlyComposable get() = LocalFalesiaColors.current

    val typography: FalesiaTypography
        @Composable @ReadOnlyComposable get() = LocalFalesiaTypography.current

    val spacing: FalesiaSpacing get() = FalesiaSpacing

    val radius: FalesiaRadius get() = FalesiaRadius

    val size: FalesiaSize get() = FalesiaSize
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme
        else -> lightScheme
    }
    val falesiaColors = if (darkTheme) darkFalesiaColors else lightFalesiaColors

    CompositionLocalProvider(
        LocalFalesiaColors provides falesiaColors,
        LocalFalesiaTypography provides DefaultFalesiaTypography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
