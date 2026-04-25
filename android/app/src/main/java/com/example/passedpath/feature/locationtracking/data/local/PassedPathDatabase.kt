package com.example.passedpath.feature.locationtracking.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.dao.TrackingDebugLogDao
import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.entity.GpsPointEntity
import com.example.passedpath.feature.locationtracking.data.local.entity.TrackingDebugLogEntity

@Database(
    entities = [GpsPointEntity::class, DayRouteEntity::class, TrackingDebugLogEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PassedPathDatabase : RoomDatabase() {
    abstract fun gpsPointDao(): GpsPointDao
    abstract fun dayRouteDao(): DayRouteDao
    abstract fun trackingDebugLogDao(): TrackingDebugLogDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tracking_debug_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recordedAtEpochMillis INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        dateKey TEXT,
                        message TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_tracking_debug_logs_recordedAtEpochMillis
                    ON tracking_debug_logs(recordedAtEpochMillis)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_tracking_debug_logs_dateKey_recordedAtEpochMillis
                    ON tracking_debug_logs(dateKey, recordedAtEpochMillis)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS day_routes_new (
                        dateKey TEXT NOT NULL PRIMARY KEY,
                        totalDistanceMeters REAL NOT NULL,
                        pathPointCount INTEGER NOT NULL,
                        lastRecordedAtEpochMillis INTEGER,
                        lastSyncedAtEpochMillis INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO day_routes_new (
                        dateKey,
                        totalDistanceMeters,
                        pathPointCount,
                        lastRecordedAtEpochMillis,
                        lastSyncedAtEpochMillis
                    )
                    SELECT
                        dateKey,
                        totalDistanceMeters,
                        pathPointCount,
                        lastRecordedAtEpochMillis,
                        lastSyncedAtEpochMillis
                    FROM day_routes
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE day_routes")
                db.execSQL("ALTER TABLE day_routes_new RENAME TO day_routes")
            }
        }
    }
}
