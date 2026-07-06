package com.example.passedpath.feature.bookmark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkItem
import com.example.passedpath.feature.bookmark.domain.usecase.GetDayRouteBookmarksUseCase
import com.example.passedpath.feature.bookmark.presentation.state.DayRouteBookmarkListUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DayRouteBookmarkListViewModel(
    private val getDayRouteBookmarksUseCase: GetDayRouteBookmarksUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DayRouteBookmarkListUiState())
    val uiState: StateFlow<DayRouteBookmarkListUiState> = _uiState.asStateFlow()

    fun fetchBookmarks(forceRefresh: Boolean = false) {
        val currentState = _uiState.value
        if (currentState.isLoading) return
        if (!forceRefresh && currentState.hasLoaded) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                getDayRouteBookmarksUseCase(size = BookmarkListPageSize)
            }.onSuccess { bookmarkList ->
                _uiState.update {
                    it.copy(
                        bookmarkCount = bookmarkList.dayRouteCount,
                        bookmarks = bookmarkList.dayRoutes,
                        hasNext = bookmarkList.hasNext,
                        nextCursorDate = bookmarkList.nextCursorDate,
                        hasLoaded = true,
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable)
                    )
                }
            }
        }
    }

    fun fetchNextBookmarks() {
        val currentState = _uiState.value
        val cursorDate = currentState.nextCursorDate ?: return
        if (
            !currentState.hasLoaded ||
            !currentState.hasNext ||
            currentState.isLoading ||
            currentState.isLoadingMore
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMore = true,
                    errorMessage = null
                )
            }

            runCatching {
                getDayRouteBookmarksUseCase(
                    cursorDate = cursorDate,
                    size = BookmarkListPageSize
                )
            }.onSuccess { bookmarkList ->
                _uiState.update { state ->
                    state.copy(
                        bookmarkCount = bookmarkList.dayRouteCount,
                        bookmarks = (state.bookmarks + bookmarkList.dayRoutes)
                            .distinctBy(DayRouteBookmarkItem::date),
                        hasNext = bookmarkList.hasNext,
                        nextCursorDate = bookmarkList.nextCursorDate,
                        hasLoaded = true,
                        isLoadingMore = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable)
                    )
                }
            }
        }
    }
}

class DayRouteBookmarkListViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayRouteBookmarkListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DayRouteBookmarkListViewModel(
                getDayRouteBookmarksUseCase = appContainer.getDayRouteBookmarksUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private const val BookmarkListPageSize = 20
