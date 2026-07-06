package com.example.passedpath.feature.bookmark.domain.usecase

import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository

class ToggleDayRouteBookmarksUseCase(
    private val dayRouteBookmarkRepository: DayRouteBookmarkRepository
) {
    suspend operator fun invoke(dateKeys: List<String>) {
        dayRouteBookmarkRepository.toggleBookmarks(dateKeys = dateKeys)
    }
}
