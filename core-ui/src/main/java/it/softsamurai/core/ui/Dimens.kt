package it.softsamurai.core.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Spacing, radius and size tokens of the design system (tokens.json → spacing, radius, size).

/** Tailwind's 4px scale, only the steps the screens use. */
@Immutable
object FalesiaSpacing {
    /** Icon to label in the bottom bar and in location lines. */
    val space1: Dp = 4.dp

    /** Horizontal padding of micro tags. */
    val space1Half: Dp = 6.dp

    /** Gaps inside chips and tags; gap between top bar actions. */
    val space2: Dp = 8.dp

    /** Gap between chips, tiles and list rows. */
    val space3: Dp = 12.dp

    /** Screen side padding, card padding in lists, top bar padding. */
    val space4: Dp = 16.dp

    /** Crag card body padding. */
    val space5: Dp = 20.dp

    /** Gap between crag cards and between sections. */
    val space6: Dp = 24.dp

    /** Bottom spacer before the bottom bar on Discover. */
    val space10: Dp = 40.dp

    /** FAB distance from the bottom edge. */
    val space24: Dp = 96.dp
}

@Immutable
object FalesiaRadius {
    /** Status and ascent tags. */
    val default: Dp = 8.dp

    /** Route list chips, grade box, thumbnails. */
    val lg: Dp = 16.dp

    /** Cards, tiles, search fields, Discover chips, buttons. */
    val xl: Dp = 24.dp
}

@Immutable
object FalesiaSize {
    val iconButton: Dp = 40.dp
    val iconButtonLg: Dp = 48.dp
    val fab: Dp = 56.dp
    val field: Dp = 56.dp
    val chip: Dp = 40.dp
    val chipSm: Dp = 36.dp
    val button: Dp = 48.dp
    val gradeBox: Dp = 64.dp
    val thumb: Dp = 48.dp

    /** Hairlines: card borders, top bar and bottom bar separators. */
    val hairline: Dp = 1.dp
}

/** `radius-full`: icon buttons, round FAB, style pills, avatars. */
val PillShape = CircleShape

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(FalesiaRadius.default),
    small = RoundedCornerShape(FalesiaRadius.default),
    medium = RoundedCornerShape(FalesiaRadius.lg),
    large = RoundedCornerShape(FalesiaRadius.xl),
    extraLarge = RoundedCornerShape(FalesiaRadius.xl),
)
