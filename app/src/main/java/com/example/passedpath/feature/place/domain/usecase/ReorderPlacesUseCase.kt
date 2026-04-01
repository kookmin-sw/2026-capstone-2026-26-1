package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.repository.PlaceRepository

class ReorderPlacesUseCase(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(dateKey: String, placeIds: List<Long>) {
        placeRepository.reorderPlaces(
            dateKey = dateKey,
            placeIds = placeIds
        )
    }
}
