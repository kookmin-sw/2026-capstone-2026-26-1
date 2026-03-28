package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail

interface DayRouteRepository {
    suspend fun getLocalDayRoute(dateKey: String): DailyPath?

    suspend fun markLocalDayRouteSynced(dateKey: String, syncedAtEpochMillis: Long)

    suspend fun refreshRemoteDayRoute(dateKey: String): DayRouteDetail
}
