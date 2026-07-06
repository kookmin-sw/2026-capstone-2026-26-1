package com.example.passedpath.feature.care.domain.repository

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteDetail
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteList

sealed interface ProtectedPersonDayRouteListResult {
    data class Success(
        val dayRouteList: ProtectedPersonDayRouteList
    ) : ProtectedPersonDayRouteListResult

    data class Error(
        val throwable: Throwable
    ) : ProtectedPersonDayRouteListResult
}

sealed interface ProtectedPersonDayRouteResult {
    data class Success(
        val routeDetail: ProtectedPersonDayRouteDetail
    ) : ProtectedPersonDayRouteResult

    data object Empty : ProtectedPersonDayRouteResult

    data class Error(
        val throwable: Throwable
    ) : ProtectedPersonDayRouteResult
}

interface ProtectedPersonDayRouteRepository {
    suspend fun fetchDayRoutes(
        dependentUserId: Long,
        cursorDate: String? = null,
        size: Int? = null
    ): ProtectedPersonDayRouteListResult

    suspend fun fetchDayRoute(
        dependentUserId: Long,
        dateKey: String
    ): ProtectedPersonDayRouteResult
}
