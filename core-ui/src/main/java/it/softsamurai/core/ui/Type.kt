package it.softsamurai.core.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val lexend = GoogleFont("Lexend")

// Lexend — the only family of the design system, in the weights the screens use (300–700 and 900)
val lexendFontFamily = FontFamily(
    Font(googleFont = lexend, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = lexend, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = lexend, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = lexend, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = lexend, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = lexend, fontProvider = provider, weight = FontWeight.Black),
)

private fun lexend(
    size: Int,
    lineHeight: Float,
    weight: FontWeight,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = lexendFontFamily,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing,
)

/**
 * The named type styles of the design system (tokens.json → `type`). Read them with
 * `FalesiaTheme.typography`. Labels, tags and overlines are uppercased by the caller, never in strings.
 */
@Immutable
data class FalesiaTypography(
    // Titles
    val heroTitle: TextStyle = lexend(30, 36f, FontWeight.Bold),
    val sectionTitle: TextStyle = lexend(24, 32f, FontWeight.Bold, (-0.025).em),
    val screenTitle: TextStyle = lexend(20, 25f, FontWeight.Bold, (-0.025).em),
    val cardTitle: TextStyle = lexend(20, 28f, FontWeight.Bold),
    val titleSm: TextStyle = lexend(18, 28f, FontWeight.Bold),
    val itemTitle: TextStyle = lexend(16, 24f, FontWeight.Bold),
    // Numbers
    val gradeXl: TextStyle = lexend(24, 32f, FontWeight.Black),
    val statXl: TextStyle = lexend(24, 32f, FontWeight.Bold),
    val tileValue: TextStyle = lexend(20, 28f, FontWeight.Bold),
    val stat: TextStyle = lexend(18, 28f, FontWeight.Bold),
    // Text
    val input: TextStyle = lexend(18, 28f, FontWeight.Normal),
    val body: TextStyle = lexend(16, 26f, FontWeight.Normal),
    val bodySm: TextStyle = lexend(14, 20f, FontWeight.Normal),
    val bodySmRelaxed: TextStyle = lexend(14, 22.75f, FontWeight.Normal),
    val button: TextStyle = lexend(16, 24f, FontWeight.Bold),
    val chip: TextStyle = lexend(14, 20f, FontWeight.Medium),
    val link: TextStyle = lexend(14, 20f, FontWeight.Medium),
    val caption: TextStyle = lexend(12, 16f, FontWeight.Medium),
    val label: TextStyle = lexend(12, 16f, FontWeight.Bold, (-0.05).em),
    val overline: TextStyle = lexend(12, 16f, FontWeight.Medium, 0.05.em),
    val tag: TextStyle = lexend(12, 16f, FontWeight.Bold, 0.05.em),
    val micro: TextStyle = lexend(10, 15f, FontWeight.Bold, 0.025.em),
)

internal val DefaultFalesiaTypography = FalesiaTypography()

internal val LocalFalesiaTypography = staticCompositionLocalOf { DefaultFalesiaTypography }

private val baseline = Typography()

// Material 3 slots mapped onto the design scale, so stock M3 components (NavigationBar, TopAppBar,
// Snackbar …) already speak the design's language.
val AppTypography = with(DefaultFalesiaTypography) {
    Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = lexendFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = lexendFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = lexendFontFamily),
        headlineLarge = heroTitle,
        headlineMedium = sectionTitle,
        headlineSmall = sectionTitle,
        titleLarge = cardTitle,
        titleMedium = titleSm,
        titleSmall = itemTitle,
        bodyLarge = body,
        bodyMedium = bodySm,
        bodySmall = lexend(12, 16f, FontWeight.Normal),
        labelLarge = chip,
        labelMedium = caption,
        labelSmall = micro,
    )
}
