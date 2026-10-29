package it.softsamurai.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Raw values of the Claude Design tokens (design/claude-design/design-system/tokens.json).
// Screens never read these directly: they go through MaterialTheme.colorScheme or FalesiaTheme.colors.

// ── Brand ──
internal val Primary = Color(0xFFF49D25)
internal val OnPrimary = Color(0xFF221A10)

// Light-theme text variant of the accent: #F49D25 is 2.0:1 on the light bg, this is 5.1:1.
// Not in the design yet (see doc/ui-compose.md, open issue on light `primary` contrast).
internal val PrimaryTextLight = Color(0xFF9A5A00)

// ── Neutrals (Tailwind slate) ──
private val Slate100 = Color(0xFFF1F5F9)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate600 = Color(0xFF475569)
private val Slate700 = Color(0xFF334155)
private val Slate900 = Color(0xFF0F172A)

internal val BgDark = Color(0xFF221A10)
internal val BgLight = Color(0xFFF8F7F5)

private fun Color.alpha(percent: Int): Color = copy(alpha = percent / 100f)

/**
 * Design tokens that have no Material 3 slot. Read them with `FalesiaTheme.colors`.
 * Names follow tokens.json (`ink-muted` → [inkMuted], `primary-10` → [primary10] …).
 */
@Immutable
data class FalesiaColors(
    /** True for the dark theme: for content that is not drawn with tokens, such as the map style. */
    val isDark: Boolean,
    val primaryText: Color,
    val primary5: Color,
    val primary10: Color,
    val primary20: Color,
    val primary30: Color,
    val primary40: Color,
    val primary60: Color,
    val bg: Color,
    val bgGlass80: Color,
    val bgGlass95: Color,
    val ink: Color,
    val inkSecondary: Color,
    val inkBody: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val inkDim: Color,
    val inkOnPhoto: Color,
    val inkOnPhotoMuted: Color,
    val surfaceCard: Color,
    val surfaceCardNeutral: Color,
    val surfaceRow: Color,
    val line: Color,
    val lineDivider: Color,
    val chip: Color,
    val chipInk: Color,
    val field: Color,
    val fieldIcon: Color,
    val fieldPlaceholder: Color,
    val navInactive: Color,
    val success: Color,
    val successSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val special: Color,
    val specialSoft: Color,
    val scrim: Color,
    /** Topo badge backdrop over a photo. */
    val overlay: Color = Color.Black.alpha(40),
    /** Pills drawn on the topo (route numbers). */
    val overlayStrong: Color = Color.Black.alpha(60),
    /** Border of the topo badge. */
    val overlayLine: Color = Color.White.alpha(10),
)

val darkFalesiaColors = FalesiaColors(
    isDark = true,
    primaryText = Primary,
    primary5 = Primary.alpha(5),
    primary10 = Primary.alpha(10),
    primary20 = Primary.alpha(20),
    primary30 = Primary.alpha(30),
    primary40 = Primary.alpha(40),
    primary60 = Primary.alpha(60),
    bg = BgDark,
    bgGlass80 = BgDark.alpha(80),
    bgGlass95 = BgDark.alpha(95),
    ink = Slate100,
    inkSecondary = Slate300,
    inkBody = Slate400,
    inkMuted = Slate400,
    inkFaint = Slate400,
    inkDim = Slate500,
    inkOnPhoto = Color.White,
    inkOnPhotoMuted = Slate200,
    surfaceCard = Primary.alpha(5),
    surfaceCardNeutral = Color.White.alpha(5),
    surfaceRow = Primary.alpha(5),
    line = Primary.alpha(10),
    lineDivider = Primary.alpha(10),
    chip = Primary.alpha(20),
    chipInk = Primary,
    field = Primary.alpha(10),
    fieldIcon = Primary.alpha(60),
    fieldPlaceholder = Primary.alpha(40),
    navInactive = Primary.alpha(40),
    success = Color(0xFF4ADE80),
    successSoft = Color(0xFF22C55E).alpha(10),
    info = Color(0xFF60A5FA),
    infoSoft = Color(0xFF3B82F6).alpha(10),
    special = Color(0xFFC084FC),
    specialSoft = Color(0xFFA855F7).alpha(10),
    scrim = BgDark.alpha(80),
)

val lightFalesiaColors = FalesiaColors(
    isDark = false,
    primaryText = PrimaryTextLight,
    primary5 = Primary.alpha(5),
    primary10 = Primary.alpha(10),
    primary20 = Primary.alpha(20),
    primary30 = Primary.alpha(30),
    primary40 = Primary.alpha(40),
    primary60 = Primary.alpha(60),
    bg = BgLight,
    bgGlass80 = BgLight.alpha(80),
    bgGlass95 = BgLight.alpha(95),
    ink = Slate900,
    inkSecondary = Slate600,
    inkBody = Slate600,
    inkMuted = Slate500,
    inkFaint = Slate400,
    inkDim = Slate500,
    inkOnPhoto = Color.White,
    inkOnPhotoMuted = Slate200,
    surfaceCard = Color.White,
    surfaceCardNeutral = Color.White,
    surfaceRow = BgLight,
    line = Slate200,
    lineDivider = Slate100,
    chip = Slate200,
    chipInk = Slate700,
    field = Slate200.alpha(50),
    fieldIcon = Slate500,
    fieldPlaceholder = Slate400,
    navInactive = Slate400,
    success = Color(0xFF16A34A),
    successSoft = Color(0xFF22C55E).alpha(10),
    info = Color(0xFF2563EB),
    infoSoft = Color(0xFF3B82F6).alpha(10),
    special = Color(0xFF9333EA),
    specialSoft = Color(0xFFA855F7).alpha(10),
    // The hero gradient keeps the dark scrim in both themes: the text over the photo is always white.
    scrim = BgDark.alpha(80),
)

internal val LocalFalesiaColors = staticCompositionLocalOf { darkFalesiaColors }
