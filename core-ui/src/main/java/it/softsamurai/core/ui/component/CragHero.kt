package it.softsamurai.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import it.softsamurai.core.ui.AppTheme
import it.softsamurai.core.ui.FalesiaTheme
import it.softsamurai.core.ui.PillShape
import it.softsamurai.core.ui.R
import java.util.Locale

/** 40% of a 390×844 phone, as in the design. */
private val HeroMinHeight = 338.dp
private val SmallIconSize = 14.dp
private const val SCRIM_TRANSPARENT_STOP = 0.5f

/**
 * Full-width photo at the top of crag details (design component `CragHero`): an optional [badge], the
 * crag [name] (`hero-title`) and its [location] over a `scrim` gradient that fades out at half height.
 * [image] draws the photo, filling the hero (e.g. a Coil `AsyncImage` with `matchParentSize()`).
 */
@Composable
fun CragHero(
    name: String,
    modifier: Modifier = Modifier,
    location: String? = null,
    badge: String? = null,
    image: @Composable BoxScope.() -> Unit = {},
) {
    val colors = FalesiaTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HeroMinHeight)
            .background(colors.primary10),
        contentAlignment = Alignment.BottomStart,
    ) {
        image()
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        SCRIM_TRANSPARENT_STOP to Color.Transparent,
                        1f to colors.scrim,
                    ),
                ),
        )
        Column(
            modifier = Modifier.padding(FalesiaTheme.spacing.space6),
            verticalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space2),
        ) {
            badge?.let { HeroBadge(it) }
            Column {
                Text(
                    text = name,
                    style = FalesiaTheme.typography.heroTitle,
                    color = colors.inkOnPhoto,
                    modifier = Modifier.semantics { heading() },
                )
                location?.takeIf { it.isNotBlank() }?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space1),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_location_on),
                            contentDescription = null,
                            tint = colors.inkOnPhotoMuted,
                            modifier = Modifier.size(SmallIconSize),
                        )
                        Text(
                            text = it,
                            style = FalesiaTheme.typography.body,
                            color = colors.inkOnPhotoMuted
                        )
                    }
                }
            }
        }
    }
}

/** Solid accent pill (`fm-tag-solid`) with a star: the crag's rating. */
@Composable
private fun HeroBadge(text: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, PillShape)
            .padding(
                horizontal = FalesiaTheme.spacing.space3,
                vertical = FalesiaTheme.spacing.space1
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FalesiaTheme.spacing.space1),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_star_filled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(SmallIconSize),
        )
        Text(
            text = text.uppercase(Locale.getDefault()),
            style = FalesiaTheme.typography.label,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

// ──────────────────────── Previews ────────────────────────

@PreviewLightDark
@Composable
private fun CragHeroPreview() {
    AppTheme {
        CragHero(
            name = "Monsummano",
            location = "Toscana Nord Ovest, Toscana",
            badge = "4.5 · 15 votes"
        )
    }
}

@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun CragHeroLargeFontPreview() {
    AppTheme {
        CragHero(name = "Monsummano", location = "Toscana Nord Ovest, Toscana")
    }
}
