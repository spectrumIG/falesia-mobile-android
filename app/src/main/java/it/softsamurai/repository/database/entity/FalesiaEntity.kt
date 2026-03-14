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
)
