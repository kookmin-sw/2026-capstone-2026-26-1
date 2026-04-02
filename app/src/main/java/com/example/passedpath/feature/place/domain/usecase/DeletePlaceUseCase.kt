package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.repository.PlaceRepository

class DeletePlaceUseCase(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(dateKey: String, placeId: Long) {
        placeRepository.deletePlace(
            dateKey = dateKey,
            placeId = placeId
        )
    }
}
