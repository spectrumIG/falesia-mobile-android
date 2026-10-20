package it.softsamurai.domain.model

// Facts about a crag derived from its sectors and routes: pure functions, shared by the screens.

private const val DEFAULT_FEATURED = 3
private const val DEFAULT_EXPOSURES = 3

/** Easiest and hardest grade of the crag, from the sectors (or the routes when no sector has grades). */
fun Falesia.gradeRange(): ClosedRange<FrenchGrade>? {
    val fromSettori =
        settori.flatMap { listOf(it.gradoMin, it.gradoMax) }.mapNotNull(FrenchGrade::parse)
    val grades = fromSettori.ifEmpty { vie.mapNotNull { FrenchGrade.parse(it.grado) } }
    val min = grades.minOrNull() ?: return null
    return min..grades.max()
}

/** Shortest and longest approach walk in minutes, across the sectors. */
fun Falesia.approachRange(): IntRange? {
    val min = settori.mapNotNull { it.approachMinMinutes ?: it.approachMaxMinutes }.minOrNull()
        ?: return null
    val max = settori.mapNotNull { it.approachMaxMinutes ?: it.approachMinMinutes }.max()
    return min..max
}

/** The directions most routes face: each sector's exposures weigh as many as its routes. */
fun Falesia.mainExposures(limit: Int = DEFAULT_EXPOSURES): List<Exposure> =
    settori
        .flatMap { settore -> settore.exposures.map { it to settore.numeroVie.coerceAtLeast(1) } }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, weights) -> weights.sum() }
        .entries
        .sortedWith(compareByDescending<Map.Entry<Exposure, Int>> { it.value }.thenBy { it.key.ordinal })
        .take(limit)
        .map { it.key }

/** The best rated routes: highest rating first, then the most voted. Routes without votes come last. */
fun Falesia.featuredVie(count: Int = DEFAULT_FEATURED): List<Via> =
    vie.sortedWith(
        compareByDescending<Via> { it.rating ?: -1.0 }.thenByDescending { it.ratingCount },
    ).take(count)

/** Where to drive to: the parking when known, otherwise the crag itself. */
fun Falesia.navigationTarget(): GeoPoint? =
    parking ?: location()

/** The crag's position, null when the site has no coordinates for it. */
fun Falesia.location(): GeoPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return GeoPoint(lat, lng)
}
