package it.softsamurai.repository.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "falesie")
data class FalesiaEntity(
    /** URL-derived slug used as stable identifier (e.g. "arco", "finale-ligure"). */
    @PrimaryKey val id: String,
    val nome: String,
    val zona: String,
    /** Absolute URL of the crag's main photo. May be null if not found during scraping. */
    val pictureUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val numeroVie: Int,
    val linkDettagli: String,
    /** Epoch millis of the last successful sync for this crag. */
    val lastUpdated: Long,
    val description: String = "",
    val rating: Double? = null,
    val ratingCount: Int = 0,
    val rock: String = "",
    val seasons: String = "",
    // ── On-demand details (RefreshCragDetailsUseCase): never written by the weekly sync ──
    val parkingLatitude: Double? = null,
    val parkingLongitude: Double? = null,
    /** Epoch millis of the last download of the routes and the parking; null if never downloaded. */
    val vieUpdatedAt: Long? = null,
)

/**
 * The columns of [FalesiaEntity] written by the weekly sync. Upserting it (`@Upsert(entity = …)`) leaves the
 * on-demand details (parking, [FalesiaEntity.vieUpdatedAt]) as they are.
 */
data class FalesiaSyncEntity(
    val id: String,
    val nome: String,
    val zona: String,
    val pictureUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val numeroVie: Int,
    val linkDettagli: String,
    val lastUpdated: Long,
    val description: String,
    val rating: Double?,
    val ratingCount: Int,
    val rock: String,
    val seasons: String,
)
