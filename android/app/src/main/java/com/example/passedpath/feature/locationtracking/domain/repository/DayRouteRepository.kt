package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.LocalDayRouteSnapshot
import kotlinx.coroutines.flow.Flow

sealed interface RemoteDayRouteResult {
    data class Success(val routeDetail: DayRouteDetail) : RemoteDayRouteResult
    data object Empty : RemoteDayRouteResult
    data class Error(val throwable: Throwable) : RemoteDayRouteResult
}

interface DayRouteRepository {
    // GPS 전체 entity 읽고 full 모델로 매핑
    fun observeLocalDayRoute(dateKey: String): Flow<DailyPath?>

    // 화면 렌더링용 observe api -> main route 화면이 사용
    fun observeLocalRouteSnapshot(dateKey: String): Flow<LocalDayRouteSnapshot?>

    suspend fun getLocalDayRoute(dateKey: String): DailyPath?

    suspend fun markLocalDayRouteSynced(dateKey: String, syncedAtEpochMillis: Long)

    suspend fun fetchRemoteDayRoute(dateKey: String): RemoteDayRouteResult
}
