package com.example.passedpath.feature.bookmark.data.repository

import com.example.passedpath.feature.bookmark.data.remote.api.DayRouteBookmarkApi
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkBatchRequestDto
import com.example.passedpath.feature.bookmark.data.remote.mapper.toDayRouteBookmarkList
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmark
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkList
import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository
import retrofit2.HttpException

class DayRouteBookmarkRepositoryImpl(
    private val dayRouteBookmarkApi: DayRouteBookmarkApi
) : DayRouteBookmarkRepository {
    override suspend fun getBookmarkedDayRoutes(
        cursorDate: String?,
        size: Int
    ): DayRouteBookmarkList {
        return dayRouteBookmarkApi.getBookmarkedDayRoutes(
            cursorDate = cursorDate,
            size = size
        ).toDayRouteBookmarkList()
    }

    override suspend fun toggleBookmark(dateKey: String): DayRouteBookmark {
        val response = dayRouteBookmarkApi.toggleBookmark(date = dateKey)
        return DayRouteBookmark(isBookmarked = response.isBookmarked)
    }

    override suspend fun toggleBookmarks(dateKeys: List<String>) {
        val response = dayRouteBookmarkApi.toggleBookmarks(
            request = DayRouteBookmarkBatchRequestDto(dates = dateKeys)
        )
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
}
