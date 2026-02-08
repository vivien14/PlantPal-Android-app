package com.example.plantpal.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.plantpal.data.dao.PlantDao
import com.example.plantpal.model.Plant

@Database(
    entities = [Plant::class],
    version = 4,
    exportSchema = false
)
abstract class PlantDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao

    companion object {
        @Volatile
        private var INSTANCE: PlantDatabase? = null

        // Migration from version 1 to 2: Add instructions column
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE plants ADD COLUMN instructions TEXT DEFAULT NULL")
            }
        }

        // Migration from version 2 to 3: Add displayOrder column with sequential values
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the column first
                database.execSQL("ALTER TABLE plants ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
                // Update existing plants to have sequential display orders based on name
                database.execSQL("""
                    UPDATE plants SET displayOrder = (
                        SELECT COUNT(*) FROM plants p2 WHERE p2.name < plants.name OR (p2.name = plants.name AND p2.id < plants.id)
                    )
                """)
            }
        }

        // Migration from version 3 to 4: Fix displayOrder for existing plants (all were 0)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Update existing plants to have sequential display orders based on name
                database.execSQL("""
                    UPDATE plants SET displayOrder = (
                        SELECT COUNT(*) FROM plants p2 WHERE p2.name < plants.name OR (p2.name = plants.name AND p2.id < plants.id)
                    )
                """)
            }
        }

        fun getDatabase(context: Context): PlantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlantDatabase::class.java,
                    "plant_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
