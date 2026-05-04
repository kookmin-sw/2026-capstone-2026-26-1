package com.example.passedpath.feature.placebookmark.domain.usecase

import com.example.passedpath.feature.placebookmark.domain.repository.PlaceBookmarkRepository

class DeletePlaceBookmarkUseCase(
    private val placeBookmarkRepository: PlaceBookmarkRepository
) {
    suspend operator fun invoke(bookmarkPlaceId: Long) {
        placeBookmarkRepository.deletePlaceBookmark(bookmarkPlaceId = bookmarkPlaceId)
    }
}
