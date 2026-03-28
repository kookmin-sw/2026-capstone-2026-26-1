package com.example.passedpath.feature.locationtracking.data.repository

import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.mapper.toDailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository

class RoomDayRouteRepository(
    private val dayRouteDao: DayRouteDao,
    private val gpsPointDao: GpsPointDao
) : DayRouteRepository {

    override suspend fun getLocalDayRoute(dateKey: String): DailyPath? {
        val route = dayRouteDao.getByDate(dateKey)
        val points = gpsPointDao.getPointsByDate(dateKey)
        if (route == null && points.isEmpty()) return null

        return points.toDailyPath(
            dateKey = dateKey,
            existingRoute = route
        )
    }

    override suspend fun markLocalDayRouteSynced(dateKey: String, syncedAtEpochMillis: Long) {
        dayRouteDao.updateLastSyncedAt(
            dateKey = dateKey,
            syncedAtEpochMillis = syncedAtEpochMillis
        )
    }

    override suspend fun refreshRemoteDayRoute(dateKey: String): DailyPath {
        error("Remote day-route refresh is planned for Issue 7. Requested dateKey=$dateKey")
    }
}
