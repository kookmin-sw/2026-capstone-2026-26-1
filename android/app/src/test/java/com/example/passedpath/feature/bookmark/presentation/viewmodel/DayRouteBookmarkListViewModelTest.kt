package com.example.passedpath.feature.bookmark.presentation.viewmodel

import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmark
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkItem
import com.example.passedpath.feature.bookmark.domain.model.DayRouteBookmarkList
import com.example.passedpath.feature.bookmark.domain.repository.DayRouteBookmarkRepository
import com.example.passedpath.feature.bookmark.domain.usecase.GetDayRouteBookmarksUseCase
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DayRouteBookmarkListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `fetchBookmarks loads first page`() = runTest {
        val repository = FakeDayRouteBookmarkRepository(
            pages = mapOf(
                null to bookmarkList(
                    routes = listOf(bookmarkItem(date = "2026-01-20")),
                    hasNext = true,
                    nextCursorDate = "2026-01-20"
                )
            )
        )
        val viewModel = createViewModel(repository)

        viewModel.fetchBookmarks()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasLoaded)
        assertFalse(state.isLoading)
        assertEquals(1, state.bookmarks.size)
        assertEquals("2026-01-20", state.bookmarks.first().date)
        assertTrue(state.hasNext)
        assertEquals("2026-01-20", state.nextCursorDate)
        assertNull(state.errorMessage)
        assertEquals(listOf(null to 20), repository.requests)
    }

    @Test
    fun `fetchNextBookmarks appends next page with cursor`() = runTest {
        val repository = FakeDayRouteBookmarkRepository(
            pages = mapOf(
                null to bookmarkList(
                    routes = listOf(bookmarkItem(date = "2026-01-20")),
                    hasNext = true,
                    nextCursorDate = "2026-01-20"
                ),
                "2026-01-20" to bookmarkList(
                    routes = listOf(bookmarkItem(date = "2026-01-12")),
                    hasNext = false,
                    nextCursorDate = null
                )
            )
        )
        val viewModel = createViewModel(repository)

        viewModel.fetchBookmarks()
        advanceUntilIdle()
        viewModel.fetchNextBookmarks()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("2026-01-20", "2026-01-12"), state.bookmarks.map { it.date })
        assertFalse(state.hasNext)
        assertNull(state.nextCursorDate)
        assertFalse(state.isLoadingMore)
        assertEquals(listOf(null to 20, "2026-01-20" to 20), repository.requests)
    }

    @Test
    fun `fetchBookmarks exposes error on first page failure`() = runTest {
        val repository = FakeDayRouteBookmarkRepository(
            failureCursorDates = setOf(null)
        )
        val viewModel = createViewModel(repository)

        viewModel.fetchBookmarks()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasLoaded)
        assertFalse(state.isLoading)
        assertTrue(state.bookmarks.isEmpty())
        assertNotNull(state.errorMessage)
    }

    private fun createViewModel(
        repository: DayRouteBookmarkRepository
    ): DayRouteBookmarkListViewModel {
        return DayRouteBookmarkListViewModel(
            getDayRouteBookmarksUseCase = GetDayRouteBookmarksUseCase(repository)
        )
    }

    private class FakeDayRouteBookmarkRepository(
        private val pages: Map<String?, DayRouteBookmarkList> = mapOf(
            null to bookmarkList()
        ),
        private val failureCursorDates: Set<String?> = emptySet()
    ) : DayRouteBookmarkRepository {
        val requests = mutableListOf<Pair<String?, Int>>()

        override suspend fun getBookmarkedDayRoutes(
            cursorDate: String?,
            size: Int
        ): DayRouteBookmarkList {
            requests += cursorDate to size
            if (cursorDate in failureCursorDates) {
                throw RuntimeException("bookmark list failed")
            }
            return pages[cursorDate] ?: bookmarkList()
        }

        override suspend fun toggleBookmark(dateKey: String): DayRouteBookmark {
            return DayRouteBookmark(isBookmarked = true)
        }

        override suspend fun toggleBookmarks(dateKeys: List<String>) = Unit
    }
}

private fun bookmarkList(
    routes: List<DayRouteBookmarkItem> = emptyList(),
    hasNext: Boolean = false,
    nextCursorDate: String? = null
): DayRouteBookmarkList {
    return DayRouteBookmarkList(
        dayRouteCount = routes.size,
        hasNext = hasNext,
        nextCursorDate = nextCursorDate,
        dayRoutes = routes
    )
}

private fun bookmarkItem(
    date: String,
    title: String? = "수업 듣고 지연이 만나러 혜화 간 날",
    visitedRegions: List<String> = listOf("하루 요약의 방문 동네 리스트")
): DayRouteBookmarkItem {
    return DayRouteBookmarkItem(
        date = date,
        title = title,
        visitedRegions = visitedRegions
    )
}
