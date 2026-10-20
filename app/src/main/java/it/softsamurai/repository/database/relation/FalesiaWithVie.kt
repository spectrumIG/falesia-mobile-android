package it.softsamurai.repository.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.database.entity.ViaEntity

data class FalesiaWithVie(
    @Embedded val falesia: FalesiaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "falesiaId",
    )
    val settori: List<SettoreEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "falesiaId",
    )
    val vie: List<ViaEntity>,
)
