package com.example.passedpath.feature.bookmark.data.remote.dto

data class DayRouteBookmarkListResponseDto(
    val dayRouteCount: Int?,
    val hasNext: Boolean?,
    val nextCursorDate: String?,
    val dayRoutes: List<DayRouteBookmarkItemResponseDto>?
)

data class DayRouteBookmarkItemResponseDto(
    val date: String?,
    val title: String?,
    val visitedRegions: List<String>?
)
