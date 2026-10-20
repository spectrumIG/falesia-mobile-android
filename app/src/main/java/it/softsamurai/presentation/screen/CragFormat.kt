package it.softsamurai.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import it.softsamurai.R
import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.GeoPoint
import it.softsamurai.domain.model.GradeBand
import it.softsamurai.domain.model.ViaStyle
import java.util.Locale
import kotlin.math.abs

// Texts derived from the domain models, shared by crag details and the route list.

@Composable
@ReadOnlyComposable
internal fun ViaStyle.label(): String? = when (this) {
    ViaStyle.Sport -> stringResource(R.string.style_sport)
    ViaStyle.Trad -> stringResource(R.string.style_trad)
    ViaStyle.Mixed -> stringResource(R.string.style_mixed)
    ViaStyle.Unknown -> null
}

@Composable
@ReadOnlyComposable
internal fun Exposure.label(): String = stringResource(
    when (this) {
        Exposure.N -> R.string.exposure_n
        Exposure.NE -> R.string.exposure_ne
        Exposure.E -> R.string.exposure_e
        Exposure.SE -> R.string.exposure_se
        Exposure.S -> R.string.exposure_s
        Exposure.SW -> R.string.exposure_sw
        Exposure.W -> R.string.exposure_w
        Exposure.NW -> R.string.exposure_nw
    },
)

@Composable
@ReadOnlyComposable
internal fun GradeBand.label(): String = stringResource(
    when (this) {
        GradeBand.UpTo5 -> R.string.grade_band_up_to_5
        GradeBand.Six -> R.string.grade_band_6
        GradeBand.Seven -> R.string.grade_band_7
        GradeBand.EightPlus -> R.string.grade_band_8_plus
    },
)

/** `20 min`, `6–15 min`, or null when unknown. */
@Composable
@ReadOnlyComposable
internal fun approachText(range: IntRange?): String? = when {
    range == null -> null
    range.first == range.last -> stringResource(R.string.approach_minutes, range.first)
    else -> stringResource(R.string.approach_minutes_range, range.first, range.last)
}

/** `43.87° N, 10.83° E`. */
@Composable
@ReadOnlyComposable
internal fun coordinatesText(point: GeoPoint): String {
    val lat = stringResource(
        if (point.latitude >= 0) R.string.latitude_north else R.string.latitude_south,
        decimal(abs(point.latitude)),
    )
    val lng = stringResource(
        if (point.longitude >= 0) R.string.longitude_east else R.string.longitude_west,
        decimal(abs(point.longitude)),
    )
    return "$lat, $lng"
}

/** `4.5` (or `4,5`), as the app's locale writes decimals. */
internal fun ratingText(rating: Double): String = String.format(Locale.getDefault(), "%.1f", rating)

@Composable
@ReadOnlyComposable
internal fun ratingDescription(rating: Double, count: Int): String =
    pluralStringResource(R.plurals.cd_rating, count, ratingText(rating), count)

private fun decimal(value: Double): String = String.format(Locale.getDefault(), "%.2f", value)
