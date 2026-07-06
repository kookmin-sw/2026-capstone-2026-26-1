package com.example.passedpath.feature.bookmark.domain.repository

import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmark
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkList

interface DayRouteBookmarkRepository {
    suspend fun getBookmarkedDayRoutes(
        cursorDate: String? = null,
        size: Int = 10
    ): DayRouteBookmarkList

    suspend fun toggleBookmark(dateKey: String): DayRouteBookmark

    suspend fun toggleBookmarks(dateKeys: List<String>)
}
