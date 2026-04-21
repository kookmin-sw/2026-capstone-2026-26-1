package com.example.passedpath.feature.bookmark.domain.repository

import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmark

interface DayRouteBookmarkRepository {
    suspend fun toggleBookmark(dateKey: String): DayRouteBookmark
}
