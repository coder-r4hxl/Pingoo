package com.pingoo.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pingoo.app.data.local.dao.DestinationDao
import com.pingoo.app.data.local.dao.JourneyDao
import com.pingoo.app.data.local.entity.DestinationEntity
import com.pingoo.app.data.local.entity.JourneyEntity
import com.pingoo.app.data.local.entity.JourneyStopEntity

@Database(
    entities = [
        DestinationEntity::class,
        JourneyEntity::class,
        JourneyStopEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao
    abstract fun journeyDao(): JourneyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pingoo_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
