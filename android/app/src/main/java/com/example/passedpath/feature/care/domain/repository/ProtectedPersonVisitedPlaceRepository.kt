package com.example.passedpath.feature.care.domain.repository

import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlaceList

sealed interface ProtectedPersonVisitedPlaceResult {
    data class Success(
        val placeList: ProtectedPersonVisitedPlaceList
    ) : ProtectedPersonVisitedPlaceResult

    data object Empty : ProtectedPersonVisitedPlaceResult

    data class Error(
        val throwable: Throwable
    ) : ProtectedPersonVisitedPlaceResult
}

interface ProtectedPersonVisitedPlaceRepository {
    suspend fun fetchVisitedPlaces(
        dependentUserId: Long,
        dateKey: String
    ): ProtectedPersonVisitedPlaceResult
}
