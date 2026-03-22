package com.example.passedpath.feature.locationtracking.data.repository

import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.mapper.epochMillisToDateKey
import com.example.passedpath.feature.locationtracking.data.local.mapper.toDailyPath
import com.example.passedpath.feature.locationtracking.data.local.mapper.toDayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.mapper.toGpsPointEntity
import com.example.passedpath.feature.locationtracking.data.local.mapper.toTrackedLocation
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.ZoneId

class RoomLocationTrackingRepository(
    private val gpsPointDao: GpsPointDao,
    private val dayRouteDao: DayRouteDao,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : LocationTrackingRepository {

    override suspend fun saveRawLocation(location: TrackedLocation) {
        val dateKey = epochMillisToDateKey(
            epochMillis = location.recordedAtEpochMillis,
            zoneId = zoneId
        )

        gpsPointDao.insert(location.toGpsPointEntity(dateKey))
        refreshDayRouteSummary(dateKey)
    }

    override fun observeDailyPath(dateKey: String): Flow<DailyPath> {
        return gpsPointDao.observePointsByDate(dateKey)
            .combine(dayRouteDao.observeByDate(dateKey)) { points, route ->
                points.toDailyPath(dateKey = dateKey, existingRoute = route)
            }
    }

    override suspend fun getPendingUploadLocations(dateKey: String, limit: Int): List<TrackedLocation> {
        return gpsPointDao.getPendingUploadPoints(dateKey, limit).map { it.toTrackedLocation() }
    }

    override suspend fun markLocationsUploaded(recordedAtEpochMillis: List<Long>) {
        if (recordedAtEpochMillis.isEmpty()) return
        gpsPointDao.markUploaded(recordedAtEpochMillis)
    }

    private suspend fun refreshDayRouteSummary(dateKey: String) {
        val previousRoute = dayRouteDao.getByDate(dateKey)
        val points = gpsPointDao.getPointsByDate(dateKey)
        val updatedRoute = points.toDayRouteEntity(
            dateKey = dateKey,
            previousRoute = previousRoute
        )
        dayRouteDao.upsert(updatedRoute)
    }
}
