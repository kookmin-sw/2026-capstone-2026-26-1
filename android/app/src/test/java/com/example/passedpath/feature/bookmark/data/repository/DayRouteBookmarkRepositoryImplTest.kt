package com.example.passedpath.feature.bookmark.data.repository

import com.example.passedpath.feature.bookmark.data.remote.api.DayRouteBookmarkApi
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkBatchRequestDto
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkItemResponseDto
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkListResponseDto
import com.example.passedpath.feature.bookmark.data.remote.dto.DayRouteBookmarkResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class DayRouteBookmarkRepositoryImplTest {

    @Test
    fun `getBookmarkedDayRoutes forwards cursor and size then maps response`() = runTest {
        val fakeApi = FakeDayRouteBookmarkApi(
            listResponse = DayRouteBookmarkListResponseDto(
                dayRouteCount = 1,
                hasNext = true,
                nextCursorDate = "2026-05-07",
                dayRoutes = listOf(
                    DayRouteBookmarkItemResponseDto(
                        date = "2026-05-09",
                        title = null,
                        visitedRegions = listOf("성북구")
                    )
                )
            )
        )
        val repository = DayRouteBookmarkRepositoryImpl(dayRouteBookmarkApi = fakeApi)

        val result = repository.getBookmarkedDayRoutes(
            cursorDate = "2026-05-10",
            size = 20
        )

        assertEquals("2026-05-10", fakeApi.receivedCursorDate)
        assertEquals(20, fakeApi.receivedSize)
        assertEquals(1, result.dayRouteCount)
        assertEquals("2026-05-09", result.dayRoutes.first().date)
        assertEquals(listOf("성북구"), result.dayRoutes.first().visitedRegions)
    }

    @Test(expected = IllegalStateException::class)
    fun `getBookmarkedDayRoutes propagates api failures`() = runTest {
        val repository = DayRouteBookmarkRepositoryImpl(
            dayRouteBookmarkApi = FakeDayRouteBookmarkApi(
                throwable = IllegalStateException("boom")
            )
        )

        repository.getBookmarkedDayRoutes(cursorDate = null, size = 10)
    }

    @Test
    fun `toggleBookmarks forwards requested dates`() = runTest {
        val fakeApi = FakeDayRouteBookmarkApi()
        val repository = DayRouteBookmarkRepositoryImpl(dayRouteBookmarkApi = fakeApi)

        repository.toggleBookmarks(listOf("2026-05-09", "2026-05-10"))

        assertEquals(
            listOf("2026-05-09", "2026-05-10"),
            fakeApi.receivedBatchRequest?.dates
        )
    }

    private class FakeDayRouteBookmarkApi(
        private val listResponse: DayRouteBookmarkListResponseDto =
            DayRouteBookmarkListResponseDto(
                dayRouteCount = 0,
                hasNext = false,
                nextCursorDate = null,
                dayRoutes = emptyList()
            ),
        private val throwable: Throwable? = null
    ) : DayRouteBookmarkApi {
        var receivedCursorDate: String? = null
        var receivedSize: Int? = null
        var receivedBatchRequest: DayRouteBookmarkBatchRequestDto? = null

        override suspend fun getBookmarkedDayRoutes(
            cursorDate: String?,
            size: Int
        ): DayRouteBookmarkListResponseDto {
            receivedCursorDate = cursorDate
            receivedSize = size
            throwable?.let { throw it }
            return listResponse
        }

        override suspend fun toggleBookmark(date: String): DayRouteBookmarkResponseDto {
            return DayRouteBookmarkResponseDto(isBookmarked = true)
        }

        override suspend fun toggleBookmarks(
            request: DayRouteBookmarkBatchRequestDto
        ): Response<Unit> {
            receivedBatchRequest = request
            return Response.success(Unit)
        }
    }
}
