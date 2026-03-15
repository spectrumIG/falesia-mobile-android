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
    ],
    indices = [Index("falesiaId")],
)
data class ViaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val falesiaId: String,
    val nome: String,
    val grado: String,
)
