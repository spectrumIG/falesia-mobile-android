package it.softsamurai.repository.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import it.softsamurai.repository.database.entity.TopoEntity
import it.softsamurai.repository.database.entity.TopoLineEntity

/** A sector's topo with its route lines. Kept out of [FalesiaWithVie]: Discover loads every crag with it. */
data class TopoWithLines(
    @Embedded val topo: TopoEntity,
    @Relation(
        parentColumn = "settoreId",
        entityColumn = "settoreId",
    )
    val lines: List<TopoLineEntity>,
)
