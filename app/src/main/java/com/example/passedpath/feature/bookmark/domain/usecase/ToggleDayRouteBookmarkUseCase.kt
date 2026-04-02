package com.example.passedpath.feature.bookmark.domain.usecase

import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmark
import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository

class ToggleDayRouteBookmarkUseCase(
    private val dayRouteBookmarkRepository: DayRouteBookmarkRepository
) {
    suspend operator fun invoke(dateKey: String): DayRouteBookmark {
        return dayRouteBookmarkRepository.toggleBookmark(dateKey = dateKey)
    }
}
