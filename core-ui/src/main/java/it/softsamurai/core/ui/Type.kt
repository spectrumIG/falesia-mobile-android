package it.softsamurai.core.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

// Lexend — the display/body font used in the Stitch design
val lexendFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Lexend"), fontProvider = provider),
)

private val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = lexendFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = lexendFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = lexendFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = lexendFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = lexendFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = lexendFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = lexendFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = lexendFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = lexendFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = lexendFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = lexendFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = lexendFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = lexendFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = lexendFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = lexendFontFamily),
)
