package com.example.passedpath.feature.placebookmark.domain.repository

import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkList

interface PlaceBookmarkRepository {
    suspend fun getPlaceBookmarks(): PlaceBookmarkList

    suspend fun updatePlaceBookmark(
        bookmarkPlaceId: Long,
        placeBookmark: PlaceBookmark
    ): PlaceBookmark

    suspend fun deletePlaceBookmark(bookmarkPlaceId: Long)
}
