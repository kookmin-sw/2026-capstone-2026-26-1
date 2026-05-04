package com.example.passedpath.feature.placebookmark.domain.usecase

import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkList
import com.example.passedpath.feature.placebookmark.domain.repository.PlaceBookmarkRepository

class GetPlaceBookmarksUseCase(
    private val placeBookmarkRepository: PlaceBookmarkRepository
) {
    suspend operator fun invoke(): PlaceBookmarkList {
        return placeBookmarkRepository.getPlaceBookmarks()
    }
}
