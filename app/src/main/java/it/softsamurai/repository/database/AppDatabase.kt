package it.softsamurai.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import it.softsamurai.repository.database.dao.FalesiaDao
import it.softsamurai.repository.database.dao.ViaDao
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.ViaEntity

@Database(
    entities = [FalesiaEntity::class, ViaEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun falesiaDao(): FalesiaDao
    abstract fun viaDao(): ViaDao
}
