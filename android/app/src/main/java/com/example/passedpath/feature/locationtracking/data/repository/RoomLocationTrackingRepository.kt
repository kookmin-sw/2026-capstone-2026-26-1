package com.example.passedpath.feature.locationtracking.data.repository

import com.example.passedpath.debug.TrackingDiagnosticsLogger
import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.mapper.distanceBetweenMeters
import com.example.passedpath.feature.locationtracking.data.local.mapper.toDailyPath
import com.example.passedpath.feature.locationtracking.data.local.mapper.toGpsPointEntity
import com.example.passedpath.feature.locationtracking.data.local.mapper.toTrackedLocation
import com.example.passedpath.feature.locationtracking.data.local.mapper.toUpdatedDayRouteEntity
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.policy.LocationPersistencePolicy
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingDateKeyResolver
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.repository.SaveRawLocationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomLocationTrackingRepository(
    private val gpsPointDao: GpsPointDao,
    private val dayRouteDao: DayRouteDao,
    private val dateKeyResolver: TrackingDateKeyResolver,
    private val diagnosticsLogger: TrackingDiagnosticsLogger
) : LocationTrackingRepository {

    override suspend fun saveRawLocation(location: TrackedLocation): SaveRawLocationResult {
        val dateKey = dateKeyResolver.resolveDateKey(location.recordedAtEpochMillis)
        val latestSavedPoint = gpsPointDao.getLatestPointByDate(dateKey)?.toTrackedLocation()

        if (
            location.accuracyMeters != null &&
            location.accuracyMeters > LocationPersistencePolicy.MAX_ACCEPTABLE_ACCURACY_METERS
        ) {
            diagnosticsLogger.log(
                category = TrackingDiagnosticsLogger.CATEGORY_SAVE,
                message = "drop_accuracy accuracy=${location.accuracyMeters} max=${LocationPersistencePolicy.MAX_ACCEPTABLE_ACCURACY_METERS}",
                dateKey = dateKey
            )
            return SaveRawLocationResult.DROPPED_ACCURACY
        }

        if (!LocationPersistencePolicy.shouldPersistLocation(latestSavedPoint, location)) {
            val movedDistanceMeters = latestSavedPoint?.let { distanceBetweenMeters(it, location) }
            diagnosticsLogger.log(
                category = TrackingDiagnosticsLogger.CATEGORY_SAVE,
                message = "drop_distance moved=$movedDistanceMeters min=${LocationPersistencePolicy.MIN_SAVE_DISTANCE_METERS}",
                dateKey = dateKey
            )
            return SaveRawLocationResult.DROPPED_DISTANCE
        }

        gpsPointDao.insert(location.toGpsPointEntity(dateKey))
        val previousRoute = dayRouteDao.getByDate(dateKey)
        dayRouteDao.upsert(
            previousRoute.toUpdatedDayRouteEntity(
                dateKey = dateKey,
                newPoint = location,
                previousPoint = latestSavedPoint
            )
        )
        diagnosticsLogger.log(
            category = TrackingDiagnosticsLogger.CATEGORY_SAVE,
            message = "saved accuracy=${location.accuracyMeters} recordedAt=${location.recordedAtEpochMillis}",
            dateKey = dateKey
        )
        return SaveRawLocationResult.SAVED
    }

    override fun observeDailyPath(dateKey: String): Flow<DailyPath> {
        return gpsPointDao.observePointsByDate(dateKey)
            .combine(dayRouteDao.observeByDate(dateKey)) { points, route ->
                points.toDailyPath(dateKey = dateKey, existingRoute = route)
            }
    }

    override suspend fun getPendingUploadLocationCount(dateKey: String): Int {
        return gpsPointDao.getPendingUploadPointCount(dateKey)
    }

    override suspend fun getPendingUploadLocations(
        dateKey: String,
        limit: Int
    ): List<TrackedLocation> {
        return gpsPointDao.getPendingUploadPoints(dateKey, limit).map { it.toTrackedLocation() }
    }

    override suspend fun markLocationsUploaded(recordedAtEpochMillis: List<Long>) {
        if (recordedAtEpochMillis.isEmpty()) return
        gpsPointDao.markUploaded(recordedAtEpochMillis)
    }
}
