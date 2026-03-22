package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath

interface DayRouteRepository {
    suspend fun getLocalDayRoute(dateKey: String): DailyPath?
    suspend fun refreshRemoteDayRoute(dateKey: String): DailyPath
}
