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
import com.example.passedpath.feature.locationtracking.domain.policy.LocationTrackingPolicy
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingDateKeyResolver
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomLocationTrackingRepository(
    private val gpsPointDao: GpsPointDao,
    private val dayRouteDao: DayRouteDao,
    private val dateKeyResolver: TrackingDateKeyResolver,
    private val diagnosticsLogger: TrackingDiagnosticsLogger
) : LocationTrackingRepository {

    override suspend fun saveRawLocation(location: TrackedLocation) {

        // "2026-03-28"
        val dateKey = dateKeyResolver.resolveDateKey(location.recordedAtEpochMillis)

        // 정책에 맞게 저장 전 필터링: 정확도가 낮으면 저장 안함
        // 현재, 정확도가 null일 때도 저장하는 구조
        if (
            location.accuracyMeters != null &&
            location.accuracyMeters > LocationTrackingPolicy.MAX_ACCEPTABLE_ACCURACY_METERS
        ) {
            diagnosticsLogger.log(
                category = TrackingDiagnosticsLogger.CATEGORY_SAVE,
                message = "drop_accuracy accuracy=${location.accuracyMeters} max=${LocationTrackingPolicy.MAX_ACCEPTABLE_ACCURACY_METERS}",
                dateKey = dateKey
            )
            return
        }

        // 정책에 맞게 저장 전 필터링: 마지막 포인트 기준으로, 이동 거리 미충족시에는 저장 안함
        val latestSavedPointEntity = gpsPointDao.getLatestPointByDate(dateKey)
        val latestSavedPoint = latestSavedPointEntity?.toTrackedLocation()
        if (latestSavedPoint != null) {
            val movedDistanceMeters = distanceBetweenMeters(latestSavedPoint, location)
            if (movedDistanceMeters < LocationTrackingPolicy.MIN_SAVE_DISTANCE_METERS) {
                diagnosticsLogger.log(
                    category = TrackingDiagnosticsLogger.CATEGORY_SAVE,
                    message = "drop_distance moved=$movedDistanceMeters min=${LocationTrackingPolicy.MIN_SAVE_DISTANCE_METERS}",
                    dateKey = dateKey
                )
                return
            }
        }

        // 실제 ROOM DB에 저장
        gpsPointDao.insert(location.toGpsPointEntity(dateKey))
        // 증분 갱신
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
