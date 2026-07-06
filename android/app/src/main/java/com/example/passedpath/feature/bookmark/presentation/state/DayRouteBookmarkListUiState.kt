package com.example.passedpath.feature.bookmark.presentation.state

import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkItem

data class DayRouteBookmarkListUiState(
    val bookmarkCount: Int = 0,
    val bookmarks: List<DayRouteBookmarkItem> = emptyList(),
    val hasNext: Boolean = false,
    val nextCursorDate: String? = null,
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null
)
