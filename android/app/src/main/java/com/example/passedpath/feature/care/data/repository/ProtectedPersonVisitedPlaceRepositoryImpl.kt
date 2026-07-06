package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.CareDependentDayRoutePlacesApi
import com.example.passedpath.feature.care.data.remote.dto.CareDayRouteErrorResponseDto
import com.example.passedpath.feature.care.data.remote.mapper.toProtectedPersonVisitedPlaceList
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceResult
import com.google.gson.Gson
import retrofit2.HttpException

class ProtectedPersonVisitedPlaceRepositoryImpl(
    private val careDependentDayRoutePlacesApi: CareDependentDayRoutePlacesApi
) : ProtectedPersonVisitedPlaceRepository {
    override suspend fun fetchVisitedPlaces(
        dependentUserId: Long,
        dateKey: String
    ): ProtectedPersonVisitedPlaceResult {
        return try {
            val response = careDependentDayRoutePlacesApi.getDayRoutePlaces(
                dependentUserId = dependentUserId,
                date = dateKey
            )
            ProtectedPersonVisitedPlaceResult.Success(
                placeList = response.toProtectedPersonVisitedPlaceList()
            )
        } catch (throwable: Throwable) {
            if (throwable.isDayRouteNotFound()) {
                ProtectedPersonVisitedPlaceResult.Empty
            } else {
                ProtectedPersonVisitedPlaceResult.Error(throwable)
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
