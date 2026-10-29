package it.softsamurai.repository.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The topo of a sector: its photo and the frame (SVG viewBox) the route lines are drawn in. Downloaded with the
 * routes (on demand) and deleted with its sector (cascade); the weekly sync upserts sectors, so it keeps it.
 */
@Entity(
    tableName = "topi",
    foreignKeys = [
        ForeignKey(
            entity = SettoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["settoreId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("falesiaId")],
)
data class TopoEntity(
    @PrimaryKey val settoreId: Long,
    val falesiaId: String,
    val imageUrl: String?,
    val viewBoxX: Double,
    val viewBoxY: Double,
    val viewBoxWidth: Double,
    val viewBoxHeight: Double,
    /** Where the photo sits in the viewBox. */
    val imageX: Double,
    val imageY: Double,
    val imageWidth: Double,
    val imageHeight: Double,
)

/** One drawn segment (pitch) of a route on its sector's topo: SVG path data in viewBox units. */
@Entity(
    tableName = "topo_lines",
    primaryKeys = ["settoreId", "viaId", "segment"],
    foreignKeys = [
        ForeignKey(
            entity = TopoEntity::class,
            parentColumns = ["settoreId"],
            childColumns = ["settoreId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TopoLineEntity(
    val settoreId: Long,
    /** The route; no foreign key, a route listed in two sectors is stored once (see `ViaEntity`). */
    val viaId: Long,
    /** Pitch number, from 1. */
    val segment: Int,
    val pathData: String,
)
