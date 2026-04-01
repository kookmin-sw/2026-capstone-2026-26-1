package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.UpdatedPlace
import com.example.passedpath.feature.place.domain.repository.PlaceRepository

class UpdatePlaceUseCase(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(
        dateKey: String,
        placeId: Long,
        placeName: String,
        roadAddress: String,
        latitude: Double,
        longitude: Double
    ): UpdatedPlace {
        return placeRepository.updatePlace(
            dateKey = dateKey,
            placeId = placeId,
            place = PlaceRegistration(
                placeName = placeName,
                roadAddress = roadAddress,
                latitude = latitude,
                longitude = longitude
            )
        )
    }
}
