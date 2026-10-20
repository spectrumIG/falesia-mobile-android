package it.softsamurai.repository.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settori",
    foreignKeys = [
        ForeignKey(
            entity = FalesiaEntity::class,
            parentColumns = ["id"],
            childColumns = ["falesiaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("falesiaId")],
)
data class SettoreEntity(
    /** falesiaonline.it sector id. */
    @PrimaryKey val id: Long,
    val falesiaId: String,
    /** Order of the sector on the crag page. */
    val position: Int,
    val nome: String,
    val numeroVie: Int,
    val gradoMin: String,
    val gradoMax: String,
    val length: String,
    val approachMinMinutes: Int?,
    val approachMaxMinutes: Int?,
    /** `Exposure` names, comma separated (`SW,S`). */
    val exposures: String,
    val rating: Double?,
    val ratingCount: Int,
    /** `ViaStyle` name. */
    val style: String,
    val latitude: Double?,
    val longitude: Double?,
    val linkDettagli: String,
)
