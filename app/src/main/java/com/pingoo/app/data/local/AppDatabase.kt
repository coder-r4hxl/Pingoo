package com.pingoo.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pingoo.app.data.local.dao.DestinationDao
import com.pingoo.app.data.local.dao.JourneyDao
import com.pingoo.app.data.local.dao.JourneyHistoryDao
import com.pingoo.app.data.local.entity.DestinationEntity
import com.pingoo.app.data.local.entity.JourneyEntity
import com.pingoo.app.data.local.entity.JourneyHistoryEntity
import com.pingoo.app.data.local.entity.JourneyStopEntity

@Database(
    entities = [
        DestinationEntity::class,
        JourneyEntity::class,
        JourneyStopEntity::class,
        JourneyHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao
    abstract fun journeyDao(): JourneyDao
    abstract fun journeyHistoryDao(): JourneyHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `journeys` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `activeStopIndex` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `journey_stops` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `journeyId` INTEGER NOT NULL,
                        `destinationId` INTEGER NOT NULL,
                        `stopOrder` INTEGER NOT NULL,
                        `isAcknowledged` INTEGER NOT NULL,
                        FOREIGN KEY(`journeyId`) REFERENCES `journeys`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`destinationId`) REFERENCES `destinations`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journey_stops_journeyId` ON `journey_stops` (`journeyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journey_stops_destinationId` ON `journey_stops` (`destinationId`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `journey_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `totalStops` INTEGER NOT NULL,
                        `reachedStops` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `durationSeconds` INTEGER NOT NULL,
                        `timestampMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pingoo_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
