package com.example.myapplication.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.model.Medicine
import com.example.myapplication.model.Inventory
import com.example.myapplication.model.MedicineSchedule
import com.example.myapplication.room.dao.MedicineDao
import com.example.myapplication.room.dao.InventoryDao
import com.example.myapplication.room.dao.MedicineScheduleDao

@Database(
    entities = [Medicine::class, Inventory::class, MedicineSchedule::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun medicineScheduleDao(): MedicineScheduleDao // Add this line

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medicine_schedule` (
                        `id` TEXT NOT NULL, 
                        `medicineId` TEXT NOT NULL, 
                        `date` TEXT NOT NULL, 
                        `shift` TEXT NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_app_db"
                ).addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}