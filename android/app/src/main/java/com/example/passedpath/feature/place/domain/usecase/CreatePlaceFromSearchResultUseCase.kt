package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.domain.model.RegisteredPlace

class CreatePlaceFromSearchResultUseCase(
    private val addPlaceUseCase: AddPlaceUseCase
) {
    suspend operator fun invoke(
        dateKey: String,
        place: PlaceSearchResult
    ): RegisteredPlace {
        return addPlaceUseCase(
            dateKey = dateKey,
            placeName = place.name,
            roadAddress = place.displayAddress,
            latitude = place.latitude,
            longitude = place.longitude
        )
    }
}
