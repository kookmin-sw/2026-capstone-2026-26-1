package com.example.passedpath.feature.locationtracking.data.repository

import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.mapper.toDailyPath
import com.example.passedpath.feature.locationtracking.data.local.mapper.toLocalDayRouteSnapshotFromCache
import com.example.passedpath.feature.locationtracking.data.local.mapper.toLocalDayRouteSnapshot
import com.example.passedpath.feature.locationtracking.data.local.mapper.withRebuiltMapPolylineCache
import com.example.passedpath.feature.locationtracking.data.remote.api.DayRouteApi
import com.example.passedpath.feature.locationtracking.data.remote.dto.DayRouteErrorResponseDto
import com.example.passedpath.feature.locationtracking.data.remote.mapper.toDayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.LocalDayRouteSnapshot
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RoomDayRouteRepository(
    private val dayRouteDao: DayRouteDao,
    private val gpsPointDao: GpsPointDao,
    private val dayRouteApi: DayRouteApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default
) : DayRouteRepository {

    override fun observeLocalDayRoute(dateKey: String): Flow<DailyPath?> {
        return gpsPointDao.observePointsByDate(dateKey)
            .combine(dayRouteDao.observeByDate(dateKey)) { points, route ->
                if (route == null && points.isEmpty()) {
                    null
                } else {
                    points.toDailyPath(
                        dateKey = dateKey,
                        existingRoute = route
                    )
                }
            }
    }

    override fun observeLocalRouteSnapshot(dateKey: String): Flow<LocalDayRouteSnapshot?> {
        return dayRouteDao.observeByDate(dateKey)
            .map { route -> loadLocalRouteSnapshot(dateKey, route) }
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

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

    override suspend fun fetchRemoteDayRoute(dateKey: String): RemoteDayRouteResult {
        return try {
            val response = dayRouteApi.getDayRoute(dateKey)
            AppDebugLogger.debug(
                DebugLogTag.ROUTE_LOAD,
                "remote route dto dateKey=$dateKey responseDate=${response.date} totalDistance=${response.totalDistance} pathPointCount=${response.pathPointCount} gpsPoints=${response.gpsPoints?.size ?: 0}"
            )
            val routeDetail = response.toDayRouteDetail(requestedDateKey = dateKey)
            AppDebugLogger.debug(
                DebugLogTag.ROUTE_LOAD,
                "remote route mapped dateKey=$dateKey decodedPoints=${routeDetail.polylinePoints.size} places=${routeDetail.places.size}"
            )
            RemoteDayRouteResult.Success(
                routeDetail = routeDetail
            )
        } catch (throwable: Throwable) {
            if (throwable.isDayRouteNotFound()) {
                RemoteDayRouteResult.Empty
            } else {
                RemoteDayRouteResult.Error(throwable)
            }
        }
    }

    private suspend fun loadLocalRouteSnapshot(
        dateKey: String,
        route: DayRouteEntity?
    ): LocalDayRouteSnapshot? {
        withContext(cpuDispatcher) {
            route?.toLocalDayRouteSnapshotFromCache()
        }?.let { snapshot ->
            return snapshot
        }

        val points = withContext(ioDispatcher) {
            gpsPointDao.getRoutePointProjectionsByDate(dateKey)
        }
        if (route == null && points.isEmpty()) return null

        val rebuiltRoute = withContext(cpuDispatcher) {
            route
                ?.takeIf { points.size == it.pathPointCount }
                ?.withRebuiltMapPolylineCache(points)
        }

        if (rebuiltRoute != null) {
            withContext(ioDispatcher) {
                dayRouteDao.upsert(rebuiltRoute)
            }
        }

        return withContext(cpuDispatcher) {
            points.toLocalDayRouteSnapshot(
                dateKey = dateKey,
                existingRoute = rebuiltRoute ?: route
            )
        }
    }
}

private fun Throwable.isDayRouteNotFound(): Boolean {
    val httpException = this as? HttpException ?: return false
    if (httpException.code() != 404) return false

    val errorBody = httpException.response()?.errorBody()?.string()
    val errorResponse = runCatching {
        Gson().fromJson(errorBody, DayRouteErrorResponseDto::class.java)
    }.getOrNull()

    return errorResponse?.code == "DAY_ROUTE_NOT_FOUND"
}
