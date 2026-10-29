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

/** What a sector page gives: its routes and, when the sector has one, its topo. */
data class SettorePageNetworkModel(
    val vie: List<ViaNetworkModel>,
    val topo: TopoNetworkModel? = null,
)

/**
 * The topo of a sector: the photo and the route lines of the page's SVG. Every coordinate is in the SVG's
 * viewBox units; the image rectangle is where the photo sits in it.
 */
data class TopoNetworkModel(
    val imageUrl: String?,
    val viewBoxX: Double,
    val viewBoxY: Double,
    val viewBoxWidth: Double,
    val viewBoxHeight: Double,
    val imageX: Double,
    val imageY: Double,
    val imageWidth: Double,
    val imageHeight: Double,
    val lines: List<TopoLineNetworkModel>,
)

/** One drawn segment of a route (one pitch of a multi-pitch route): SVG path data in viewBox units. */
data class TopoLineNetworkModel(
    val viaId: Long,
    /** Pitch number, from 1 (`route_<viaId>_<segment>`). */
    val segment: Int,
    val pathData: String,
)
