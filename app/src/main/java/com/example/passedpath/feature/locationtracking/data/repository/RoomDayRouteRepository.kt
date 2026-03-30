package com.example.passedpath.feature.locationtracking.data.repository

import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.mapper.toDailyPath
import com.example.passedpath.feature.locationtracking.data.remote.api.DayRouteApi
import com.example.passedpath.feature.locationtracking.data.remote.dto.DayRouteErrorResponseDto
import com.example.passedpath.feature.locationtracking.data.remote.mapper.toDayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.google.gson.Gson
import retrofit2.HttpException

class RoomDayRouteRepository(
    private val dayRouteDao: DayRouteDao,
    private val gpsPointDao: GpsPointDao,
    private val dayRouteApi: DayRouteApi
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

    override suspend fun fetchRemoteDayRoute(dateKey: String): RemoteDayRouteResult {
        return try {
            RemoteDayRouteResult.Success(
                routeDetail = dayRouteApi.getDayRoute(dateKey).toDayRouteDetail(requestedDateKey = dateKey)
            )
        } catch (throwable: Throwable) {
            if (throwable.isDayRouteNotFound()) {
                RemoteDayRouteResult.Empty
            } else {
                RemoteDayRouteResult.Error(throwable)
            }
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
