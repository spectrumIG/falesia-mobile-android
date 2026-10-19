package it.softsamurai.repository.network

import com.fleeksoft.ksoup.nodes.Document
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel

// Pure HTML → network model parsing for falesiaonline.it pages. All selector knowledge lives here, apart
// from the HTTP calls in FalesiaNetDataSourceImpl, so it can be tested on saved pages (src/test/resources).

private const val BASE_HOST = "https://www.falesiaonline.it"

private val SET_VIEW_PATTERN = Regex("""setView\(\s*\[\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*]""")
private val MARKER_PATTERN = Regex("""L\.marker\(\s*\[\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*]""")

/** Parses the crag list page (POST response): one item per `li.leafletshadow`. */
internal fun parseElencoFalesie(document: Document): List<FalesiaListItemNetworkModel> =
    document.select("li.leafletshadow").map { element ->
        val nome = element.selectFirst("h4 a")?.text() ?: "N/A"
        val routeCount =
            element
                .selectFirst("div.indicatorrow.firstrow span.indicator[title=Vie]")
                ?.text()
                ?.trim()
                ?.toIntOrNull() ?: 0
        val relativeLink = element.selectFirst("h4 a")?.attr("href") ?: ""
        val detailUrl = "$BASE_HOST$relativeLink"

        // The list page has no crag thumbnail — only small icon images. The photo
        // is fetched from the detail page and takes priority in the mapper.
        val pictureUrl: String? = null

        FalesiaListItemNetworkModel(
            nome = nome,
            pictureUrl = pictureUrl,
            routeCount = routeCount,
            detailUrl = detailUrl,
        )
    }

/** Parses a crag detail page: routes, photo and coordinates. */
internal fun parseDettagliFalesia(document: Document): FalesiaDetailNetworkModel {
    val vie = document.select("div.route-details").map { element ->
        val grado = element.selectFirst("div.route-grade")?.text() ?: ""
        val nome = element.selectFirst("span.nome-via")?.text() ?: ""
        ViaNetworkModel(nome = nome, grado = grado)
    }

    // The crag photo is inside <div id="objectPhoto"><img src="..."></div>
    val rawSrc = document.selectFirst("#objectPhoto img")?.attr("src").orEmpty()
    val pictureUrl = resolveImageUrl(rawSrc)

    val (latitude, longitude) = document.extractCoordinates() ?: (null to null)

    return FalesiaDetailNetworkModel(
        pictureUrl = pictureUrl,
        latitude = latitude,
        longitude = longitude,
        vie = vie,
    )
}

/**
 * Attempts to extract geographic coordinates from Leaflet.js initialization patterns.
 *
 * Strategies (tried in order):
 * 1. `data-lat` / `data-lng` attributes on any element.
 * 2. `setView([lat, lng], zoom)` script pattern.
 * 3. `L.marker([lat, lng])` script pattern.
 */
internal fun Document.extractCoordinates(): Pair<Double, Double>? {
    // Strategy 1: data attributes
    selectFirst("[data-lat][data-lng]")?.let { el ->
        val lat = el.attr("data-lat").toDoubleOrNull()
        val lng = el.attr("data-lng").toDoubleOrNull()
        if (lat != null && lng != null) return lat to lng
    }

    select("script").forEach { script ->
        val data = script.data()

        // Strategy 2: setView, then Strategy 3: L.marker
        (SET_VIEW_PATTERN.find(data) ?: MARKER_PATTERN.find(data))?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) return lat to lng
        }
    }

    return null
}

/**
 * Converts a raw `src` attribute value (possibly relative) into an absolute URL.
 * Returns null when [rawSrc] is blank (image element not found).
 *
 * We do this manually because ksoup's `abs:src` pseudo-attribute requires the
 * parser's base URI to be set by the HTTP client — which is not guaranteed.
 */
internal fun resolveImageUrl(rawSrc: String): String? = when {
    rawSrc.startsWith("http://") || rawSrc.startsWith("https://") -> rawSrc
    rawSrc.startsWith("/") -> "$BASE_HOST$rawSrc"
    rawSrc.isNotBlank() -> "$BASE_HOST/$rawSrc"
    else -> null
}
