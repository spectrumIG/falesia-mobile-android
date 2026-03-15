package it.softsamurai.repository.network.model

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
 * Rich crag info scraped from the individual crag detail page.
 */
data class FalesiaDetailNetworkModel(
    /** Main crag photo URL. Falls back to list-page thumbnail if null. */
    val pictureUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val vie: List<ViaNetworkModel>,
)

data class ViaNetworkModel(
    val nome: String,
    val grado: String,
)
