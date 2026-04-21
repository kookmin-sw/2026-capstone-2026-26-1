package com.example.passedpath.feature.locationtracking.domain.usecase

import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.debug.TrackingDiagnosticsLogger
import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.dao.TrackingDebugLogDao
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CleanupTrackingLocalDataUseCase(
    private val gpsPointDao: GpsPointDao,
    private val dayRouteDao: DayRouteDao,
    private val trackingDebugLogDao: TrackingDebugLogDao,
    private val diagnosticsLogger: TrackingDiagnosticsLogger,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    suspend operator fun invoke(nowEpochMillis: Long = System.currentTimeMillis()) {
        cleanupRouteCache(nowEpochMillis)
        cleanupTrackingDebugLogs(nowEpochMillis)
    }

    private suspend fun cleanupRouteCache(nowEpochMillis: Long) {
        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId).toLocalDate()
        val syncedCutoffDateKey = today.minusDays(SYNCED_RETENTION_DAYS.toLong()).format(DateFormatter)
        val unsyncedCutoffDateKey = today.minusDays(UNSYNCED_RETENTION_DAYS.toLong()).format(DateFormatter)

        val syncedDateKeys = dayRouteDao.getSyncedDateKeysOlderThan(syncedCutoffDateKey)
        val unsyncedDateKeys = dayRouteDao.getUnsyncedDateKeysOlderThan(unsyncedCutoffDateKey)

        syncedDateKeys.forEach { dateKey ->
            deleteRouteCacheForDate(
                dateKey = dateKey,
                reason = "cleanup_synced"
            )
        }
        unsyncedDateKeys.forEach { dateKey ->
            deleteRouteCacheForDate(
                dateKey = dateKey,
                reason = "cleanup_unsynced"
            )
        }
    }

    private suspend fun deleteRouteCacheForDate(dateKey: String, reason: String) {
        val deletedGpsPoints = gpsPointDao.deleteByDate(dateKey)
        val deletedDayRoutes = dayRouteDao.deleteByDate(dateKey)
        diagnosticsLogger.log(
            category = TrackingDiagnosticsLogger.CATEGORY_CLEANUP,
            message = "$reason dateKey=$dateKey gpsPoints=$deletedGpsPoints dayRoutes=$deletedDayRoutes",
            dateKey = dateKey
        )
    }

    private suspend fun cleanupTrackingDebugLogs(nowEpochMillis: Long) {
        val cutoffEpochMillis =
            nowEpochMillis - DEBUG_LOG_RETENTION_DAYS * DAY_IN_MILLIS
        val deletedLogs = trackingDebugLogDao.deleteOlderThan(cutoffEpochMillis)
        if (deletedLogs > 0) {
            AppDebugLogger.debug(
                DebugLogTag.TRACKING_DIAGNOSTICS,
                "[cleanup] deleted_tracking_debug_logs=$deletedLogs cutoff=$cutoffEpochMillis"
            )
        }
    }

    companion object {
        private const val SYNCED_RETENTION_DAYS = 3
        private const val UNSYNCED_RETENTION_DAYS = 14
        private const val DEBUG_LOG_RETENTION_DAYS = 1
        private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
