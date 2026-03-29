package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail

sealed interface RemoteDayRouteResult {
    data class Success(val routeDetail: DayRouteDetail) : RemoteDayRouteResult
    data object Empty : RemoteDayRouteResult
    data class Error(val throwable: Throwable) : RemoteDayRouteResult
}

interface DayRouteRepository {
    suspend fun getLocalDayRoute(dateKey: String): DailyPath?

    suspend fun markLocalDayRouteSynced(dateKey: String, syncedAtEpochMillis: Long)

    suspend fun fetchRemoteDayRoute(dateKey: String): RemoteDayRouteResult
}
