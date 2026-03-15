package it.softsamurai.repository.mapper

import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel

// ── Entity → Domain ──────────────────────────────────────────────────────────

fun FalesiaWithVie.toDomain(): Falesia = Falesia(
    id = falesia.id,
    nome = falesia.nome,
    zona = falesia.zona,
    pictureUrl = falesia.pictureUrl,
    latitude = falesia.latitude,
    longitude = falesia.longitude,
    numeroVie = falesia.numeroVie,
    linkDettagli = falesia.linkDettagli,
    vie = vie.map { it.toDomain() },
    lastUpdated = falesia.lastUpdated,
)

fun ViaEntity.toDomain(): Via = Via(
    id = id,
    nome = nome,
    grado = grado,
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
)

fun Via.toEntity(falesiaId: String): ViaEntity = ViaEntity(
    id = id,
    falesiaId = falesiaId,
    nome = nome,
    grado = grado,
)

// ── Network → Entity ─────────────────────────────────────────────────────────

/**
 * Derives the crag ID from its URL (last path segment).
 * e.g. "https://www.falesiaonline.it/falesia/arco" → "arco"
 */
fun FalesiaListItemNetworkModel.idFromUrl(): String =
    detailUrl.trimEnd('/').substringAfterLast('/')

fun FalesiaListItemNetworkModel.toEntity(
    detail: FalesiaDetailNetworkModel,
    now: Long,
): FalesiaEntity = FalesiaEntity(
    id = idFromUrl(),
    nome = nome,
    zona = "",          // zone not available from current scraping — TODO when site exposes it
    pictureUrl = detail.pictureUrl ?: pictureUrl,
    latitude = detail.latitude,
    longitude = detail.longitude,
    numeroVie = routeCount,
    linkDettagli = detailUrl,
    lastUpdated = now,
)

fun ViaNetworkModel.toEntity(falesiaId: String): ViaEntity = ViaEntity(
    falesiaId = falesiaId,
    nome = nome,
    grado = grado,
)
