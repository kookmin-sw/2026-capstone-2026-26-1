package com.example.passedpath.feature.bookmark.data.remote.mapper

import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkItemResponseDto
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkListResponseDto
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkItem
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkList

internal fun DayRouteBookmarkListResponseDto.toDayRouteBookmarkList(): DayRouteBookmarkList {
    val mappedRoutes = dayRoutes.orEmpty().mapNotNull { item ->
        item.toDayRouteBookmarkItemOrNull()
    }

    return DayRouteBookmarkList(
        dayRouteCount = dayRouteCount ?: mappedRoutes.size,
        hasNext = hasNext == true,
        nextCursorDate = nextCursorDate.takeIf { hasNext == true },
        dayRoutes = mappedRoutes
    )
}

private fun DayRouteBookmarkItemResponseDto.toDayRouteBookmarkItemOrNull(): DayRouteBookmarkItem? {
    val dateKey = date?.takeIf { it.isNotBlank() } ?: return null
    return DayRouteBookmarkItem(
        date = dateKey,
        title = title,
        visitedRegions = visitedRegions.orEmpty()
    )
}
