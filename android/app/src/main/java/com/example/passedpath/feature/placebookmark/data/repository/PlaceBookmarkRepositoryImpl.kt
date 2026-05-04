package com.example.passedpath.feature.placebookmark.data.repository

import com.example.passedpath.feature.placebookmark.data.remote.api.PlaceBookmarkApi
import com.example.passedpath.feature.placebookmark.data.remote.mapper.toPlaceBookmarkList
import com.example.passedpath.feature.placebookmark.data.remote.mapper.toPlaceBookmark
import com.example.passedpath.feature.placebookmark.data.remote.mapper.toUpdateRequestDto
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkList
import com.example.passedpath.feature.placebookmark.domain.repository.PlaceBookmarkRepository
import retrofit2.HttpException

class PlaceBookmarkRepositoryImpl(
    private val placeBookmarkApi: PlaceBookmarkApi
) : PlaceBookmarkRepository {
    override suspend fun getPlaceBookmarks(): PlaceBookmarkList {
        return placeBookmarkApi.getPlaceBookmarks().toPlaceBookmarkList()
    }

    override suspend fun updatePlaceBookmark(
        bookmarkPlaceId: Long,
        placeBookmark: PlaceBookmark
    ): PlaceBookmark {
        return placeBookmarkApi.updatePlaceBookmark(
            bookmarkPlaceId = bookmarkPlaceId,
            request = placeBookmark.toUpdateRequestDto()
        ).toPlaceBookmark()
    }

    override suspend fun deletePlaceBookmark(bookmarkPlaceId: Long) {
        val response = placeBookmarkApi.deletePlaceBookmark(bookmarkPlaceId = bookmarkPlaceId)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
}
