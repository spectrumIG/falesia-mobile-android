package it.softsamurai.repository.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vie",
    foreignKeys = [
        ForeignKey(
            entity = FalesiaEntity::class,
            parentColumns = ["id"],
            childColumns = ["falesiaId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SettoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["settoreId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("falesiaId"), Index("settoreId")],
)
data class ViaEntity(
    /** falesiaonline.it route id. */
    @PrimaryKey val id: Long,
    val falesiaId: String,
    val settoreId: Long,
    /** Order of the route on the sector page. */
    val position: Int,
    val nome: String,
    val grado: String,
    val rating: Double?,
    val ratingCount: Int,
    val length: String,
    /** `ViaStyle` name. */
    val style: String,
)
