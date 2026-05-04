package com.example.passedpath.feature.placebookmark.domain.usecase

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import com.example.passedpath.feature.placebookmark.domain.repository.PlaceBookmarkRepository

class UpdatePlaceBookmarkUseCase(
    private val placeBookmarkRepository: PlaceBookmarkRepository
) {
    suspend operator fun invoke(
        bookmarkPlaceId: Long,
        type: BookmarkPlaceType,
        placeName: String,
        roadAddress: String,
        latitude: Double,
        longitude: Double
    ): PlaceBookmark {
        return placeBookmarkRepository.updatePlaceBookmark(
            bookmarkPlaceId = bookmarkPlaceId,
            placeBookmark = PlaceBookmark(
                type = type,
                placeName = placeName,
                roadAddress = roadAddress,
                latitude = latitude,
                longitude = longitude
            )
        )
    }
}
