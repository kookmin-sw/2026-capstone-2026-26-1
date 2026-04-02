package com.example.passedpath.feature.bookmark.data.repository

import com.example.passedpath.feature.bookmark.data.remote.api.DayRouteBookmarkApi
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmark
import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository

class DayRouteBookmarkRepositoryImpl(
    private val dayRouteBookmarkApi: DayRouteBookmarkApi
) : DayRouteBookmarkRepository {
    override suspend fun toggleBookmark(dateKey: String): DayRouteBookmark {
        val response = dayRouteBookmarkApi.toggleBookmark(date = dateKey)
        return DayRouteBookmark(isBookmarked = response.isBookmarked)
    }
}
