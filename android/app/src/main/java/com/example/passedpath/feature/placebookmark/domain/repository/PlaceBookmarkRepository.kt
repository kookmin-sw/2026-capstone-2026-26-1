package com.example.passedpath.feature.placebookmark.domain.repository

import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark

interface PlaceBookmarkRepository {
    suspend fun updatePlaceBookmark(
        bookmarkPlaceId: Long,
        placeBookmark: PlaceBookmark
    ): PlaceBookmark
}
