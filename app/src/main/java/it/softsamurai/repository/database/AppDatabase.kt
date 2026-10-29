package it.softsamurai.repository.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import it.softsamurai.repository.database.dao.FalesiaDao
import it.softsamurai.repository.database.dao.SettoreDao
import it.softsamurai.repository.database.dao.TopoDao
import it.softsamurai.repository.database.dao.ViaDao
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.database.entity.TopoEntity
import it.softsamurai.repository.database.entity.TopoLineEntity
import it.softsamurai.repository.database.entity.ViaEntity

@Database(
    entities = [
        FalesiaEntity::class,
        SettoreEntity::class,
        ViaEntity::class,
        TopoEntity::class,
        TopoLineEntity::class,
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 3, to = 4, spec = AppDatabase.Migration3To4::class)],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun falesiaDao(): FalesiaDao
    abstract fun settoreDao(): SettoreDao
    abstract fun viaDao(): ViaDao
    abstract fun topoDao(): TopoDao

    /**
     * 3 → 4 adds the topo tables. The routes already saved have no topo: forget when they were downloaded, so
     * the next opening of each crag downloads routes and topi together.
     */
    class Migration3To4 : AutoMigrationSpec {
        override fun onPostMigrate(connection: SQLiteConnection) {
            connection.execSQL("UPDATE falesie SET vieUpdatedAt = NULL")
        }
    }
}
