package it.softsamurai.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/** The theme carries the values of design/claude-design/design-system/tokens.json. */
class ThemeTokensTest {

    // ── Brand ──

    @Test
    fun `primary fill and on-primary match the design in both themes`() {
        listOf(lightScheme, darkScheme).forEach { scheme ->
            assertEquals(Color(0xFFF49D25), scheme.primary)
            assertEquals(Color(0xFF221A10), scheme.onPrimary)
        }
    }

    @Test
    fun `accent text is the brand orange in dark and the darker variant in light`() {
        assertEquals(Color(0xFFF49D25), darkFalesiaColors.primaryText)
        assertEquals(Color(0xFF9A5A00), lightFalesiaColors.primaryText)
    }

    // ── Surfaces and text ──

    @Test
    fun `dark theme bg and ink match the design`() {
        assertEquals(Color(0xFF221A10), darkScheme.background)
        assertEquals(Color(0xFF221A10), darkScheme.surface)
        assertEquals(Color(0xFFF1F5F9), darkScheme.onSurface)
        assertEquals(Color(0xFF94A3B8), darkScheme.onSurfaceVariant)
    }

    @Test
    fun `light theme bg and ink match the design`() {
        assertEquals(Color(0xFFF8F7F5), lightScheme.background)
        assertEquals(Color(0xFFF8F7F5), lightScheme.surface)
        assertEquals(Color(0xFF0F172A), lightScheme.onSurface)
        assertEquals(Color(0xFF64748B), lightScheme.onSurfaceVariant)
    }

    @Test
    fun `glass backgrounds are bg at 80 and 95 percent`() {
        assertEquals(Color(0xCC221A10), darkFalesiaColors.bgGlass80)
        assertEquals(Color(0xF2221A10), darkFalesiaColors.bgGlass95)
        assertEquals(Color(0xCCF8F7F5), lightFalesiaColors.bgGlass80)
        assertEquals(Color(0xF2F8F7F5), lightFalesiaColors.bgGlass95)
    }

    @Test
    fun `crag card tokens match the design`() {
        assertEquals(Color(0x0DF49D25), darkFalesiaColors.surfaceCard)
        assertEquals(Color(0x1AF49D25), darkFalesiaColors.line)
        assertEquals(Color.White, lightFalesiaColors.surfaceCard)
        assertEquals(Color(0xFFE2E8F0), lightFalesiaColors.line)
        assertEquals(Color(0xFFF1F5F9), lightFalesiaColors.lineDivider)
    }

    @Test
    fun `chip and bottom bar tokens match the design`() {
        assertEquals(Color(0x33F49D25), darkFalesiaColors.chip)
        assertEquals(Color(0xFFF49D25), darkFalesiaColors.chipInk)
        assertEquals(Color(0xFFE2E8F0), lightFalesiaColors.chip)
        assertEquals(Color(0xFF334155), lightFalesiaColors.chipInk)
        assertEquals(Color(0x66F49D25), darkFalesiaColors.navInactive)
        assertEquals(Color(0xFF94A3B8), lightFalesiaColors.navInactive)
    }

    // ── Type ──

    @Test
    fun `named type styles match the design scale`() {
        with(FalesiaTypography()) {
            assertEquals(24f, sectionTitle.fontSize.value)
            assertEquals(32f, sectionTitle.lineHeight.value)
            assertEquals(20f, screenTitle.fontSize.value)
            assertEquals(25f, screenTitle.lineHeight.value)
            assertEquals(20f, cardTitle.fontSize.value)
            assertEquals(18f, stat.fontSize.value)
            assertEquals(12f, label.fontSize.value)
            assertEquals(900, gradeXl.fontWeight?.weight)
        }
    }

    @Test
    fun `material slots reuse the design styles`() {
        assertEquals(DefaultFalesiaTypography.cardTitle, AppTypography.titleLarge)
        assertEquals(DefaultFalesiaTypography.body, AppTypography.bodyLarge)
        assertEquals(DefaultFalesiaTypography.caption, AppTypography.labelMedium)
    }

    // ── Spacing, radius, size ──

    @Test
    fun `spacing radius and size tokens match the design`() {
        assertEquals(16.dp, FalesiaSpacing.space4)
        assertEquals(20.dp, FalesiaSpacing.space5)
        assertEquals(24.dp, FalesiaSpacing.space6)
        assertEquals(8.dp, FalesiaRadius.default)
        assertEquals(16.dp, FalesiaRadius.lg)
        assertEquals(24.dp, FalesiaRadius.xl)
        assertEquals(40.dp, FalesiaSize.iconButton)
        assertEquals(56.dp, FalesiaSize.field)
        assertEquals(40.dp, FalesiaSize.chip)
    }
}
