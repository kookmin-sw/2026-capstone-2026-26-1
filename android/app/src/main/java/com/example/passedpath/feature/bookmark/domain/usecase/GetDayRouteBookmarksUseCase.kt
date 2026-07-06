package com.example.passedpath.feature.bookmark.domain.usecase

import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkList
import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository

class GetDayRouteBookmarksUseCase(
    private val dayRouteBookmarkRepository: DayRouteBookmarkRepository
) {
    suspend operator fun invoke(
        cursorDate: String? = null,
        size: Int = 10
    ): DayRouteBookmarkList {
        return dayRouteBookmarkRepository.getBookmarkedDayRoutes(
            cursorDate = cursorDate,
            size = size
        )
    }
}
