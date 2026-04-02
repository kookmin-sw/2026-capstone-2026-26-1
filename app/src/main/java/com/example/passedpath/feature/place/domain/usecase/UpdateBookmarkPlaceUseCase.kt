package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.model.BookmarkPlace
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.place.domain.repository.PlaceRepository

class UpdateBookmarkPlaceUseCase(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(
        bookmarkPlaceId: Long,
        type: BookmarkPlaceType,
        placeName: String,
        roadAddress: String,
        latitude: Double,
        longitude: Double
    ): BookmarkPlace {
        return placeRepository.updateBookmarkPlace(
            bookmarkPlaceId = bookmarkPlaceId,
            bookmarkPlace = BookmarkPlace(
                type = type,
                placeName = placeName,
                roadAddress = roadAddress,
                latitude = latitude,
                longitude = longitude
            )
        )
    }
}
