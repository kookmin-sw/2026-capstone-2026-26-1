package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.CareDependentDayRouteSummaryApi
import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteErrorResponseDto
import com.example.passedpath.feature.care.data.remote.mapper.toProtectedPersonDaySummary
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryResult
import com.google.gson.Gson
import retrofit2.HttpException

class ProtectedPersonDaySummaryRepositoryImpl(
    private val careDependentDayRouteSummaryApi: CareDependentDayRouteSummaryApi
) : ProtectedPersonDaySummaryRepository {
    override suspend fun fetchDaySummary(
        dependentUserId: Long,
        dateKey: String
    ): ProtectedPersonDaySummaryResult {
        return try {
            val response = careDependentDayRouteSummaryApi.getDayRouteSummary(
                dependentUserId = dependentUserId,
                date = dateKey
            )
            ProtectedPersonDaySummaryResult.Success(
                daySummary = response.toProtectedPersonDaySummary(dateKey = dateKey)
            )
        } catch (throwable: Throwable) {
            if (throwable.isDayRouteNotFound()) {
                ProtectedPersonDaySummaryResult.Empty
            } else {
                ProtectedPersonDaySummaryResult.Error(throwable)
            }
        }
    }
}

private fun Throwable.isDayRouteNotFound(): Boolean {
    val httpException = this as? HttpException ?: return false
    if (httpException.code() != 404) return false

    val errorBody = httpException.response()?.errorBody()?.string()
    val errorResponse = runCatching {
        Gson().fromJson(errorBody, CareDayRouteErrorResponseDto::class.java)
    }.getOrNull()

    return errorResponse?.code == DayRouteNotFoundCode
}

private const val DayRouteNotFoundCode = "DAY_ROUTE_NOT_FOUND"
