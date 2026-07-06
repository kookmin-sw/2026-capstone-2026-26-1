package com.example.passedpath.feature.bookmark.domain.model

data class DayRouteBookmarkList(
    val dayRouteCount: Int,
    val hasNext: Boolean,
    val nextCursorDate: String?,
    val dayRoutes: List<DayRouteBookmarkItem>
)

data class DayRouteBookmarkItem(
    val date: String,
    val title: String?,
    val visitedRegions: List<String>
)
