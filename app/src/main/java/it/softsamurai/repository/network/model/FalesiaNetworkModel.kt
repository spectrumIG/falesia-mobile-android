package it.softsamurai.repository.network.model

import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.ViaStyle

/**
 * Lightweight crag info scraped from the list page (POST request).
 * Does NOT include routes or detailed geo data — those come from [FalesiaDetailNetworkModel].
 */
data class FalesiaListItemNetworkModel(
    val nome: String,
    /** Thumbnail URL extracted from the list page <img> tag. May be null. */
    val pictureUrl: String?,
    val routeCount: Int,
    /** Absolute URL to the crag's detail page. Also used to derive the crag ID. */
    val detailUrl: String,
)

/**
 * Rich crag info scraped from the individual crag detail page. The routes are not on it: they are on the
 * sector pages ([SettoreNetworkModel.detailUrl]).
 */
data class FalesiaDetailNetworkModel(
    /** Main crag photo URL. Falls back to list-page thumbnail if null. */
    val pictureUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val zona: String = "",
    val description: String = "",
    val rating: Double? = null,
    val ratingCount: Int = 0,
    val rock: String = "",
    val seasons: String = "",
    val settori: List<SettoreNetworkModel> = emptyList(),
)

/** A sector as listed on the crag page. */
data class SettoreNetworkModel(
    val id: Long,
    val nome: String,
    val detailUrl: String,
    val routeCount: Int,
    val gradoMin: String,
    val gradoMax: String,
    val length: String,
    val approachMinMinutes: Int?,
    val approachMaxMinutes: Int?,
    val exposures: List<Exposure>,
    val rating: Double?,
    val ratingCount: Int,
    val style: ViaStyle,
    val latitude: Double?,
    val longitude: Double?,
)

/** A route as listed on a sector page. */
data class ViaNetworkModel(
    val id: Long,
    val nome: String,
    val grado: String,
    val rating: Double? = null,
    val ratingCount: Int = 0,
    val length: String = "",
    val style: ViaStyle = ViaStyle.Unknown,
)
