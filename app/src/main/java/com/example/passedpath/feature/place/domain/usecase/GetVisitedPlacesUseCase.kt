package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.model.VisitedPlaceList
import com.example.passedpath.feature.place.domain.repository.PlaceRepository

class GetVisitedPlacesUseCase(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(dateKey: String): VisitedPlaceList {
        return placeRepository.getPlaces(dateKey = dateKey)
    }
}
