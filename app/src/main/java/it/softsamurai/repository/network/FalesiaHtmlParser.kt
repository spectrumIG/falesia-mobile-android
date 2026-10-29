package it.softsamurai.repository.network

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.SettoreNetworkModel
import it.softsamurai.repository.network.model.SettorePageNetworkModel
import it.softsamurai.repository.network.model.TopoLineNetworkModel
import it.softsamurai.repository.network.model.TopoNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel

// Pure HTML → network model parsing for falesiaonline.it pages. All selector knowledge lives here, apart
// from the HTTP calls in FalesiaNetDataSourceImpl, so it can be tested on saved pages (src/test/resources).
// Regexes run on Android's ICU engine, stricter than the JVM one: escape literal `{ } [ ]` (a bare `}`
// crashes the class at load time). FalesiaHtmlParserDeviceTest checks them on a device.

private const val BASE_HOST = "https://www.falesiaonline.it"
private const val HALF_STAR = 0.5

private val SET_VIEW_PATTERN = Regex("""setView\(\s*\[\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*\]""")
private val MARKER_PATTERN = Regex("""L\.marker\(\s*\[\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*\]""")

/** A sector marker of the crag map: position, options, then a click handler that opens the sector. */
private val SECTOR_MARKER_PATTERN = Regex(
    """L\.marker\(\s*\[\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*\]\s*,\s*\{[^\}]*\}\s*\)\s*""" +
            """\.addTo\(map\)\s*\.on\(\s*'click'\s*,\s*function\s*\(\)\s*\{\s*""" +
            """location\.href\s*=\s*'/sector_topo_sketch_or_photo/(\d+)'""",
)
private val PARKING_LAT_PATTERN = Regex("""parkingLat\s*=\s*(-?\d+\.?\d*)""")
private val PARKING_LNG_PATTERN = Regex("""parkingLong\s*=\s*(-?\d+\.?\d*)""")

/** `/settorefoto/1510/pilastro-rosso.html`, `/settoredisegno/1/ingresso.html` … → 1510, 1. */
private val SECTOR_ID_PATTERN = Regex("""/settore[a-z_]*/(\d+)/""")
private const val ROUTE_ELEMENT_PREFIX = "routeslist_element_"
private val MINUTES_PATTERN = Regex("""(\d+)(?:\s*[–-]\s*(\d+))?\s*min""")
private val LENGTH_PATTERN = Regex("""\d+(?:\s*[–-]\s*\d+)?\s*m\b""")

/** Topo SVG `viewBox="0 0 936.141 528.686"`: min x, min y, width, height. */
private val VIEW_BOX_SEPARATOR = Regex("""[\s,]+""")
private const val VIEW_BOX_VALUES = 4
private const val VIEW_BOX_WIDTH = 2
private const val VIEW_BOX_HEIGHT = 3

/** Topo photo `transform="matrix(a b c d e f)"`: scale a, d and translation e, f (no rotation on the site). */
private val MATRIX_PATTERN = Regex("""matrix\(([^)]*)\)""")
private const val MATRIX_VALUES = 6
private const val MATRIX_SCALE_Y = 3
private const val MATRIX_TRANSLATE_X = 4
private const val MATRIX_TRANSLATE_Y = 5

/** A route line of the topo: `route_<route id>_<pitch>`. */
private val ROUTE_PATH_ID_PATTERN = Regex("""^route_(\d+)_(\d+)$""")

/** Abseil lines are hidden on the site (`display: none`) and are not routes. */
private const val ABSEIL_CLASS = "abseil"
private const val DISPLAY_NONE = "display:none"

/** Breadcrumb levels skipped before the zone: the continent ("Sud Europa"). */
private const val BREADCRUMB_CONTINENT_LEVELS = 1

/** How many breadcrumb levels make the zone: area and region. */
private const val ZONE_LEVELS = 2

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

/**
 * The crag list, or [SyncError.ParseFailure] when the page has no crag: the site answers errors (e.g. a
 * "502 Bad Gateway" page) with a normal HTML body, which must not count as an empty but successful sync.
 */
internal fun parseElencoFalesieOrFail(document: Document): Either<SyncError, List<FalesiaListItemNetworkModel>> =
    either {
        parseElencoFalesie(document).also {
            ensure(it.isNotEmpty()) { SyncError.ParseFailure("Crag list page without crags") }
        }
    }

/**
 * Parses a crag detail page: description, zone, rating, photo, coordinates and the list of sectors. A page
 * without the crag title (`h1`) is not a crag page: [SyncError.ParseFailure].
 */
internal fun parseDettagliFalesia(document: Document): Either<SyncError, FalesiaDetailNetworkModel> =
    either {
        ensureNotNull(document.selectFirst("h1")) { SyncError.ParseFailure("Crag page without title") }
        val header = document.selectFirst("header.pageHeader")

    // The crag photo is inside <div id="objectPhoto"><img src="..."></div>
    val rawSrc = document.selectFirst("#objectPhoto img")?.attr("src").orEmpty()

        val markers = document.sectorMarkers()
        val settori = document.select("li.sectorleaflet").mapNotNull { it.toSettore(markers) }
        val (latitude, longitude) = markers.values.centre() ?: document.extractCoordinates()
        ?: (null to null)
        val (rating, ratingCount) = header?.selectFirst("div.stars").parseStars()

        FalesiaDetailNetworkModel(
            pictureUrl = resolveImageUrl(rawSrc),
        latitude = latitude,
        longitude = longitude,
            zona = document.parseZona(),
            description = document.select("article.narrow p, article.wide p")
                .map { it.text().trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n\n"),
            rating = rating,
            ratingCount = ratingCount,
            rock = header?.selectFirst(".rock_icon")?.text()?.trim().orEmpty(),
            seasons = header?.selectFirst(".season_icon")?.text()?.trim().orEmpty(),
            settori = settori,
        )
    }

/**
 * Parses a sector page (`/settorefoto/…`, `/settoredisegno/…`): its routes, in the site's order. A page
 * without the route list is not a sector page: [SyncError.ParseFailure].
 */
internal fun parseVieSettore(document: Document): Either<SyncError, List<ViaNetworkModel>> =
    either {
        val list = ensureNotNull(document.selectFirst("#routes_list")) {
            SyncError.ParseFailure("Sector page without route list")
        }
        list.select("div.routelistinnerelement").mapNotNull { element ->
            val id = element.id().removePrefix(ROUTE_ELEMENT_PREFIX).toLongOrNull()
                ?: return@mapNotNull null
            // Descents ("Accesso/Discesa n.1", grade "Calata") are in the list but are not routes.
            if (element.selectFirst(".descentroutename") != null) return@mapNotNull null
            val nameBlock = element.selectFirst(".starsandroutename")
            val details = element.selectFirst(".routedetails")
            val detailsText = details?.ownText()?.replace(' ', ' ')?.trim().orEmpty()
            val (rating, ratingCount) = nameBlock?.selectFirst("div.stars").parseStars()
            ViaNetworkModel(
                id = id,
                nome = nameBlock?.ownText()?.trim().orEmpty(),
                grado = detailsText.substringBefore(' ').trim(),
                rating = rating,
                ratingCount = ratingCount,
                length = LENGTH_PATTERN.find(detailsText)?.value?.replace(" ", "").orEmpty(),
                style = details.protectionStyle(),
            )
        }
    }

/** Parses a sector page: its routes ([parseVieSettore]) and its topo ([parseTopo]), from one download. */
internal fun parseSettore(document: Document): Either<SyncError, SettorePageNetworkModel> =
    parseVieSettore(document).map { vie ->
        SettorePageNetworkModel(
            vie = vie,
            topo = parseTopo(document)
        )
    }

/**
 * Parses the topo of a sector page: the SVG `#topoSvgContent` with the photo (`g#Foto image`, placed by its
 * `transform` matrix) and one path per pitch (`g#Route_Paths path#route_<id>_<pitch>`). Abseil lines are left
 * out. Null when the page has no topo or its viewBox is unreadable.
 */
internal fun parseTopo(document: Document): TopoNetworkModel? {
    val svg = document.selectFirst("svg#topoSvgContent") ?: return null
    val viewBox =
        svg.attr("viewBox").trim().split(VIEW_BOX_SEPARATOR).mapNotNull { it.toDoubleOrNull() }
    if (viewBox.size != VIEW_BOX_VALUES || viewBox[VIEW_BOX_WIDTH] <= 0.0 || viewBox[VIEW_BOX_HEIGHT] <= 0.0) {
        return null
    }
    val image = svg.selectFirst("g#Foto image") ?: svg.selectFirst("image")
    val href = image?.attr("xlink:href")?.ifBlank { image.attr("href") }.orEmpty()
    val matrix = image?.attr("transform").parseMatrix()
    val lines = svg.select("path[id^=route_]").mapNotNull { path ->
        val hidden = path.hasClass(ABSEIL_CLASS) || path.attr("style").replace(" ", "")
            .contains(DISPLAY_NONE)
        val match = ROUTE_PATH_ID_PATTERN.find(path.id())
        val pathData = path.attr("d").trim()
        if (hidden || match == null || pathData.isEmpty()) return@mapNotNull null
        TopoLineNetworkModel(
            viaId = match.groupValues[1].toLong(),
            segment = match.groupValues[2].toInt(),
            pathData = pathData,
        )
    }
    return TopoNetworkModel(
        imageUrl = resolveImageUrl(href),
        viewBoxX = viewBox[0],
        viewBoxY = viewBox[1],
        viewBoxWidth = viewBox[VIEW_BOX_WIDTH],
        viewBoxHeight = viewBox[VIEW_BOX_HEIGHT],
        imageX = matrix.translateX,
        imageY = matrix.translateY,
        // Without its own size the photo fills the viewBox.
        imageWidth = image?.attr("width")?.toDoubleOrNull()?.times(matrix.scaleX)
            ?: viewBox[VIEW_BOX_WIDTH],
        imageHeight = image?.attr("height")?.toDoubleOrNull()?.times(matrix.scaleY)
            ?: viewBox[VIEW_BOX_HEIGHT],
        lines = lines,
    )
}

/** Parses the crag map page (`/falesia/map/…`): the parking position, null when the crag has none. */
internal fun parseParcheggio(document: Document): Pair<Double, Double>? {
    document.select("script").forEach { script ->
        val data = script.data()
        val lat = PARKING_LAT_PATTERN.find(data)?.groupValues?.get(1)?.toDoubleOrNull()
        val lng = PARKING_LNG_PATTERN.find(data)?.groupValues?.get(1)?.toDoubleOrNull()
        if (lat != null && lng != null) return lat to lng
    }
    return null
}

/** The map page of a crag: `/falesia/monsummano.html` → `/falesia/map/monsummano.html`. */
internal fun mapUrlFromDetailUrl(detailUrl: String): String =
    detailUrl.replace("/falesia/", "/falesia/map/")

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

/** Links on the site are relative to the host; resolved like image sources. */
private fun absoluteUrl(href: String): String = resolveImageUrl(href).orEmpty()

/** Italian compass abbreviations used by the site (`SO` = south-west, `O` = west). */
internal fun parseExposures(text: String): List<Exposure> =
    text.split('-', ',', '/', ' ')
        .mapNotNull { token ->
            when (token.trim().uppercase()) {
                "N" -> Exposure.N
                "NE" -> Exposure.NE
                "E" -> Exposure.E
                "SE" -> Exposure.SE
                "S" -> Exposure.S
                "SO", "SW" -> Exposure.SW
                "O", "W" -> Exposure.W
                "NO", "NW" -> Exposure.NW
                else -> null
            }
        }
        .distinct()

// ── Helpers ──────────────────────────────────────────────────────────────

/** Sector id → marker position, from the crag map script. */
private fun Document.sectorMarkers(): Map<Long, Pair<Double, Double>> =
    select("script").flatMap { script -> SECTOR_MARKER_PATTERN.findAll(script.data()) }
        .mapNotNull { match ->
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            val id = match.groupValues[3].toLongOrNull()
            if (lat != null && lng != null && id != null) id to (lat to lng) else null
        }
        .toMap()

private fun Collection<Pair<Double, Double>>.centre(): Pair<Double, Double>? =
    if (isEmpty()) null else sumOf { it.first } / size to sumOf { it.second } / size

/** `Sud Europa › Italia › Toscana › Toscana Nord Ovest › Monsummano` → `Toscana Nord Ovest, Toscana`. */
private fun Document.parseZona(): String {
    val levels = select("#bread_crumb li").map { it.text().trim() }.filter { it.isNotEmpty() }
    return levels
        .drop(BREADCRUMB_CONTINENT_LEVELS)
        .dropLast(1)
        .reversed()
        .take(ZONE_LEVELS)
        .joinToString(", ")
}

private fun Element.toSettore(markers: Map<Long, Pair<Double, Double>>): SettoreNetworkModel? {
    val link = selectFirst("h4 a") ?: return null
    val href = link.attr("href")
    val id = SECTOR_ID_PATTERN.find(href)?.groupValues?.get(1)?.toLongOrNull() ?: return null
    val firstRow = selectFirst("div.indicatorrow.firstrow")
    // The grade indicator is the one without title: "6a+<div title='Grado medio 7a'/>7c+".
    val grades = firstRow?.select("span.indicator")
        ?.firstOrNull { !it.hasAttr("title") }
        ?.textNodes()
        ?.map { it.text().trim() }
        ?.filter { it.isNotEmpty() }
        .orEmpty()
    val approach = indicator("Avvicinamento")?.let(MINUTES_PATTERN::find)
    val (rating, ratingCount) = selectFirst("div.stars").parseStars()
    val position = markers[id]
    return SettoreNetworkModel(
        id = id,
        nome = link.text().trim(),
        detailUrl = absoluteUrl(href),
        routeCount = indicator("Vie")?.toIntOrNull() ?: 0,
        gradoMin = grades.firstOrNull().orEmpty(),
        gradoMax = grades.lastOrNull().orEmpty(),
        length = indicator("Sviluppo delle vie").orEmpty(),
        approachMinMinutes = approach?.groupValues?.get(1)?.toIntOrNull(),
        approachMaxMinutes = approach?.let { it.groupValues[2].ifEmpty { it.groupValues[1] } }
            ?.toIntOrNull(),
        exposures = parseExposures(indicator("Esposizione").orEmpty()),
        rating = rating,
        ratingCount = ratingCount,
        style = selectFirst("span.indicator.protection").protectionStyle(),
        latitude = position?.first,
        longitude = position?.second,
    )
}

private fun Element.indicator(title: String): String? =
    selectFirst("span.indicator[title=\"$title\"]")?.text()?.trim()?.takeIf { it.isNotEmpty() }

/** Stars widget: full and half star images, then the number of votes as text. Null rating without votes. */
private fun Element?.parseStars(): Pair<Double?, Int> {
    if (this == null) return null to 0
    val votes = ownText().trim().toIntOrNull() ?: 0
    if (votes == 0) return null to 0
    val full = select("img[src*=star_full]").size
    val half = select("img[src*=star_half]").size
    return (full + half * HALF_STAR) to votes
}

/** Scale and translation of an SVG `matrix(a b c d e f)` transform. */
private data class ScaleTranslate(
    val scaleX: Double = 1.0,
    val scaleY: Double = 1.0,
    val translateX: Double = 0.0,
    val translateY: Double = 0.0,
)

/** A missing or unreadable `matrix(…)` is the identity. */
private fun String?.parseMatrix(): ScaleTranslate {
    val values = this?.let(MATRIX_PATTERN::find)?.groupValues?.get(1)
        ?.trim()?.split(VIEW_BOX_SEPARATOR)?.mapNotNull { it.toDoubleOrNull() }
    if (values == null || values.size != MATRIX_VALUES) return ScaleTranslate()
    return ScaleTranslate(
        scaleX = values[0],
        scaleY = values[MATRIX_SCALE_Y],
        translateX = values[MATRIX_TRANSLATE_X],
        translateY = values[MATRIX_TRANSLATE_Y],
    )
}

/** Protection icon (`/images/protezioni/bolted_small.png`, title "Ancoraggi fissi") → route style. */
private fun Element?.protectionStyle(): ViaStyle {
    val icon = this?.selectFirst("img[src*=protezioni]") ?: return ViaStyle.Unknown
    val name = icon.attr("src").substringAfterLast('/').lowercase()
    return when {
        name.startsWith("bolted") -> ViaStyle.Sport
        name.startsWith("mixed") || name.startsWith("misto") -> ViaStyle.Mixed
        name.startsWith("trad") || name.startsWith("clean") || name.startsWith("natural") -> ViaStyle.Trad
        else -> ViaStyle.Unknown
    }
}
