package it.softsamurai.repository.mapper

import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.GeoPoint
import it.softsamurai.domain.model.Settore
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.FalesiaSyncEntity
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.SettoreNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel

private const val EXPOSURE_SEPARATOR = ","

// ── Entity → Domain ──────────────────────────────────────────────────────────

fun FalesiaWithVie.toDomain(): Falesia {
    val settoriOrder = settori.associate { it.id to it.position }
    return Falesia(
        id = falesia.id,
        nome = falesia.nome,
        zona = falesia.zona,
        pictureUrl = falesia.pictureUrl,
        latitude = falesia.latitude,
        longitude = falesia.longitude,
        numeroVie = falesia.numeroVie,
        linkDettagli = falesia.linkDettagli,
        // Room relations have no order: sort as on the site, by sector then by route.
        vie = vie.sortedWith(
            compareBy(
                { settoriOrder[it.settoreId] ?: Int.MAX_VALUE },
                { it.position })
        )
            .map { it.toDomain() },
        lastUpdated = falesia.lastUpdated,
        description = falesia.description,
        rating = falesia.rating,
        ratingCount = falesia.ratingCount,
        rock = falesia.rock,
        seasons = falesia.seasons,
        parking = geoPointOf(falesia.parkingLatitude, falesia.parkingLongitude),
        settori = settori.sortedBy { it.position }.map { it.toDomain() },
        vieUpdatedAt = falesia.vieUpdatedAt,
    )
}

fun SettoreEntity.toDomain(): Settore = Settore(
    id = id,
    nome = nome,
    numeroVie = numeroVie,
    gradoMin = gradoMin,
    gradoMax = gradoMax,
    length = length,
    approachMinMinutes = approachMinMinutes,
    approachMaxMinutes = approachMaxMinutes,
    exposures = exposures.split(EXPOSURE_SEPARATOR)
        .mapNotNull { name -> Exposure.entries.find { it.name == name } },
    rating = rating,
    ratingCount = ratingCount,
    style = styleOf(style),
    location = geoPointOf(latitude, longitude),
    linkDettagli = linkDettagli,
)

fun ViaEntity.toDomain(): Via = Via(
    id = id,
    nome = nome,
    grado = grado,
    settoreId = settoreId,
    rating = rating,
    ratingCount = ratingCount,
    length = length,
    style = styleOf(style),
)

// ── Domain → Entity ──────────────────────────────────────────────────────────

fun Falesia.toEntity(): FalesiaEntity = FalesiaEntity(
    id = id,
    nome = nome,
    zona = zona,
    pictureUrl = pictureUrl,
    latitude = latitude,
    longitude = longitude,
    numeroVie = numeroVie,
    linkDettagli = linkDettagli,
    lastUpdated = lastUpdated,
    description = description,
    rating = rating,
    ratingCount = ratingCount,
    rock = rock,
    seasons = seasons,
    parkingLatitude = parking?.latitude,
    parkingLongitude = parking?.longitude,
    vieUpdatedAt = vieUpdatedAt,
)

fun Settore.toEntity(falesiaId: String, position: Int): SettoreEntity = SettoreEntity(
    id = id,
    falesiaId = falesiaId,
    position = position,
    nome = nome,
    numeroVie = numeroVie,
    gradoMin = gradoMin,
    gradoMax = gradoMax,
    length = length,
    approachMinMinutes = approachMinMinutes,
    approachMaxMinutes = approachMaxMinutes,
    exposures = exposures.joinToString(EXPOSURE_SEPARATOR) { it.name },
    rating = rating,
    ratingCount = ratingCount,
    style = style.name,
    latitude = location?.latitude,
    longitude = location?.longitude,
    linkDettagli = linkDettagli,
)

fun Via.toEntity(falesiaId: String, position: Int = 0): ViaEntity = ViaEntity(
    id = id,
    falesiaId = falesiaId,
    settoreId = settoreId,
    position = position,
    nome = nome,
    grado = grado,
    rating = rating,
    ratingCount = ratingCount,
    length = length,
    style = style.name,
)

// ── Network → Entity ─────────────────────────────────────────────────────────

/**
 * Derives the crag ID from its URL (last path segment, without extension).
 * e.g. "https://www.falesiaonline.it/falesia/arco.html" → "arco"
 */
fun FalesiaListItemNetworkModel.idFromUrl(): String =
    detailUrl.trimEnd('/').substringAfterLast('/').removeSuffix(".html")

fun FalesiaListItemNetworkModel.toEntity(
    detail: FalesiaDetailNetworkModel,
    now: Long,
): FalesiaSyncEntity = FalesiaSyncEntity(
    id = idFromUrl(),
    nome = nome,
    zona = detail.zona,
    pictureUrl = detail.pictureUrl ?: pictureUrl,
    latitude = detail.latitude,
    longitude = detail.longitude,
    numeroVie = routeCount,
    linkDettagli = detailUrl,
    lastUpdated = now,
    description = detail.description,
    rating = detail.rating,
    ratingCount = detail.ratingCount,
    rock = detail.rock,
    seasons = detail.seasons,
)

fun SettoreNetworkModel.toEntity(falesiaId: String, position: Int): SettoreEntity = SettoreEntity(
    id = id,
    falesiaId = falesiaId,
    position = position,
    nome = nome,
    numeroVie = routeCount,
    gradoMin = gradoMin,
    gradoMax = gradoMax,
    length = length,
    approachMinMinutes = approachMinMinutes,
    approachMaxMinutes = approachMaxMinutes,
    exposures = exposures.joinToString(EXPOSURE_SEPARATOR) { it.name },
    rating = rating,
    ratingCount = ratingCount,
    style = style.name,
    latitude = latitude,
    longitude = longitude,
    linkDettagli = detailUrl,
)

fun ViaNetworkModel.toEntity(falesiaId: String, settoreId: Long, position: Int): ViaEntity =
    ViaEntity(
        id = id,
    falesiaId = falesiaId,
        settoreId = settoreId,
        position = position,
    nome = nome,
    grado = grado,
        rating = rating,
        ratingCount = ratingCount,
        length = length,
        style = style.name,
)

private fun geoPointOf(latitude: Double?, longitude: Double?): GeoPoint? =
    if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null

private fun styleOf(name: String): ViaStyle =
    ViaStyle.entries.find { it.name == name } ?: ViaStyle.Unknown
