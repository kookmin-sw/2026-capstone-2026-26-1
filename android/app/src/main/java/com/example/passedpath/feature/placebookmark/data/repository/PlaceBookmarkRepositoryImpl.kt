package com.example.passedpath.feature.placebookmark.data.repository

import com.example.passedpath.feature.placebookmark.data.remote.api.PlaceBookmarkApi
import com.example.passedpath.feature.placebookmark.data.remote.mapper.toPlaceBookmark
import com.example.passedpath.feature.placebookmark.data.remote.mapper.toUpdateRequestDto
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import com.example.passedpath.feature.placebookmark.domain.repository.PlaceBookmarkRepository

class PlaceBookmarkRepositoryImpl(
    private val placeBookmarkApi: PlaceBookmarkApi
) : PlaceBookmarkRepository {
    override suspend fun updatePlaceBookmark(
        bookmarkPlaceId: Long,
        placeBookmark: PlaceBookmark
    ): PlaceBookmark {
        return placeBookmarkApi.updatePlaceBookmark(
            bookmarkPlaceId = bookmarkPlaceId,
            request = placeBookmark.toUpdateRequestDto()
        ).toPlaceBookmark()
    }
}
