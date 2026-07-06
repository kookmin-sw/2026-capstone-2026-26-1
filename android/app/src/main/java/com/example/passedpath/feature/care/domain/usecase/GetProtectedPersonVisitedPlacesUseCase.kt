package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceResult

class GetProtectedPersonVisitedPlacesUseCase(
    private val repository: ProtectedPersonVisitedPlaceRepository
) {
    suspend operator fun invoke(
        dependentUserId: Long,
        dateKey: String
    ): ProtectedPersonVisitedPlaceResult {
        return repository.fetchVisitedPlaces(
            dependentUserId = dependentUserId,
            dateKey = dateKey
        )
    }
}
