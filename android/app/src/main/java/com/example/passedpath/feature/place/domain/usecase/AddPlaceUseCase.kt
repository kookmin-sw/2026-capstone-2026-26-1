package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.RegisteredPlace
import com.example.passedpath.feature.place.domain.repository.PlaceRepository

class AddPlaceUseCase(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(
        dateKey: String,
        placeName: String,
        roadAddress: String,
        latitude: Double,
        longitude: Double
    ): RegisteredPlace {
        return placeRepository.addPlace(
            dateKey = dateKey,
            place = PlaceRegistration(
                placeName = placeName,
                roadAddress = roadAddress,
                latitude = latitude,
                longitude = longitude
            )
        )
    }
}
