package com.l9rins.trademate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Add Job::class to entities
// 2. Bump version to 2
@Database(entities = [Client::class, Job::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun jobDao(): JobDao // Register the new DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trademate_local_db"
                )
                    .fallbackToDestructiveMigration() // Wipes DB if version changes (Safe for dev)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}