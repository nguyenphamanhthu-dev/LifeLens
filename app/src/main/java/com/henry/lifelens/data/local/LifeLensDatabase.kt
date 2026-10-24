package com.henry.lifelens.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScanEntity::class,
        ReminderEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LifeLensDatabase : RoomDatabase() {


    abstract fun scanDao(): ScanDao

    abstract fun reminderDao(): ReminderDao




    companion object {

        @Volatile
        private var INSTANCE: LifeLensDatabase? = null

        fun getDatabase(
            context: Context
        ): LifeLensDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeLensDatabase::class.java,
                    "lifelens_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}