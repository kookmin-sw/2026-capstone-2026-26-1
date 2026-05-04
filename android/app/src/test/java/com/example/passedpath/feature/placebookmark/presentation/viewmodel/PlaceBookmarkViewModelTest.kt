package com.example.passedpath.feature.placebookmark.presentation.viewmodel

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkList
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary
import com.example.passedpath.feature.placebookmark.domain.model.RegisteredPlaceBookmark
import com.example.passedpath.feature.placebookmark.domain.repository.PlaceBookmarkRepository
import com.example.passedpath.feature.placebookmark.domain.usecase.CreatePlaceBookmarkUseCase
import com.example.passedpath.feature.placebookmark.domain.usecase.DeletePlaceBookmarkUseCase
import com.example.passedpath.feature.placebookmark.domain.usecase.GetPlaceBookmarksUseCase
import com.example.passedpath.feature.placebookmark.domain.usecase.UpdatePlaceBookmarkUseCase
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceBookmarkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `updatePlaceBookmark replaces local item and emits changed event on success`() = runTest {
        val repository = FakePlaceBookmarkRepository(
            placeBookmarkList = PlaceBookmarkList(
                placeCount = 1,
                bookmarkPlaces = listOf(placeBookmarkSummary(bookmarkPlaceId = 7L))
            ),
            updateResult = PlaceBookmark(
                type = BookmarkPlaceType.SCHOOL,
                placeName = "Kookmin University",
                roadAddress = "Seoul Seongbuk-gu 77",
                latitude = 37.6109,
                longitude = 126.997
            )
        )
        val viewModel = createViewModel(repository)
        viewModel.fetchPlaceBookmarks()
        advanceUntilIdle()

        val changedEvent = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.placeBookmarkChanged.first()
        }
        viewModel.updatePlaceBookmark(
            bookmarkPlaceId = 7L,
            type = BookmarkPlaceType.SCHOOL,
            placeName = "Kookmin University",
            roadAddress = "Seoul Seongbuk-gu 77",
            latitude = 37.6109,
            longitude = 126.997
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals(7L, repository.updatedBookmarkPlaceId)
        assertEquals("Kookmin University", repository.updatedPlaceBookmark?.placeName)
        assertEquals(1, state.placeCount)
        assertEquals("Kookmin University", state.bookmarkPlaces.first().placeName)
        assertEquals(BookmarkPlaceType.SCHOOL, state.bookmarkPlaces.first().type)
        assertNotNull(state.successMessage)
        assertNull(state.errorMessage)
        assertEquals(7L, changedEvent.await())
    }

    @Test
    fun `updatePlaceBookmark keeps existing list and exposes error on failure`() = runTest {
        val repository = FakePlaceBookmarkRepository(
            placeBookmarkList = PlaceBookmarkList(
                placeCount = 1,
                bookmarkPlaces = listOf(placeBookmarkSummary(bookmarkPlaceId = 7L))
            ),
            updateFailure = RuntimeException("update failed")
        )
        val viewModel = createViewModel(repository)
        viewModel.fetchPlaceBookmarks()
        advanceUntilIdle()

        viewModel.updatePlaceBookmark(
            bookmarkPlaceId = 7L,
            type = BookmarkPlaceType.SCHOOL,
            placeName = "Kookmin University",
            roadAddress = "Seoul Seongbuk-gu 77",
            latitude = 37.6109,
            longitude = 126.997
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("Home", state.bookmarkPlaces.first().placeName)
        assertNotNull(state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun `deletePlaceBookmark removes local item and emits changed event on success`() = runTest {
        val repository = FakePlaceBookmarkRepository(
            placeBookmarkList = PlaceBookmarkList(
                placeCount = 2,
                bookmarkPlaces = listOf(
                    placeBookmarkSummary(bookmarkPlaceId = 7L),
                    placeBookmarkSummary(bookmarkPlaceId = 8L, placeName = "Office")
                )
            )
        )
        val viewModel = createViewModel(repository)
        viewModel.fetchPlaceBookmarks()
        advanceUntilIdle()

        val changedEvent = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.placeBookmarkChanged.first()
        }
        viewModel.deletePlaceBookmark(bookmarkPlaceId = 7L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals(7L, repository.deletedBookmarkPlaceId)
        assertEquals(1, state.placeCount)
        assertEquals(listOf(8L), state.bookmarkPlaces.map { it.bookmarkPlaceId })
        assertNotNull(state.successMessage)
        assertNull(state.errorMessage)
        assertEquals(7L, changedEvent.await())
    }

    @Test
    fun `deletePlaceBookmark keeps existing list and exposes error on failure`() = runTest {
        val repository = FakePlaceBookmarkRepository(
            placeBookmarkList = PlaceBookmarkList(
                placeCount = 1,
                bookmarkPlaces = listOf(placeBookmarkSummary(bookmarkPlaceId = 7L))
            ),
            deleteFailure = RuntimeException("delete failed")
        )
        val viewModel = createViewModel(repository)
        viewModel.fetchPlaceBookmarks()
        advanceUntilIdle()

        viewModel.deletePlaceBookmark(bookmarkPlaceId = 7L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals(1, state.placeCount)
        assertEquals(7L, state.bookmarkPlaces.first().bookmarkPlaceId)
        assertNotNull(state.errorMessage)
        assertNull(state.successMessage)
    }

    private fun createViewModel(
        repository: FakePlaceBookmarkRepository
    ): PlaceBookmarkViewModel {
        return PlaceBookmarkViewModel(
            getPlaceBookmarksUseCase = GetPlaceBookmarksUseCase(repository),
            createPlaceBookmarkUseCase = CreatePlaceBookmarkUseCase(repository),
            updatePlaceBookmarkUseCase = UpdatePlaceBookmarkUseCase(repository),
            deletePlaceBookmarkUseCase = DeletePlaceBookmarkUseCase(repository)
        )
    }

    private class FakePlaceBookmarkRepository(
        private val placeBookmarkList: PlaceBookmarkList = PlaceBookmarkList(
            placeCount = 0,
            bookmarkPlaces = emptyList()
        ),
        private val updateResult: PlaceBookmark = PlaceBookmark(
            type = BookmarkPlaceType.HOME,
            placeName = "Home",
            roadAddress = "Seoul Gangnam-gu 123",
            latitude = 37.498,
            longitude = 127.027
        ),
        private val updateFailure: Throwable? = null,
        private val deleteFailure: Throwable? = null
    ) : PlaceBookmarkRepository {
        var updatedBookmarkPlaceId: Long? = null
        var updatedPlaceBookmark: PlaceBookmark? = null
        var deletedBookmarkPlaceId: Long? = null

        override suspend fun getPlaceBookmarks(): PlaceBookmarkList {
            return placeBookmarkList
        }

        override suspend fun createPlaceBookmark(
            placeBookmark: PlaceBookmark
        ): RegisteredPlaceBookmark {
            return RegisteredPlaceBookmark(
                bookmarkPlaceId = 1L,
                type = placeBookmark.type,
                placeName = placeBookmark.placeName,
                roadAddress = placeBookmark.roadAddress,
                latitude = placeBookmark.latitude,
                longitude = placeBookmark.longitude
            )
        }

        override suspend fun updatePlaceBookmark(
            bookmarkPlaceId: Long,
            placeBookmark: PlaceBookmark
        ): PlaceBookmark {
            updateFailure?.let { throw it }
            updatedBookmarkPlaceId = bookmarkPlaceId
            updatedPlaceBookmark = placeBookmark
            return updateResult
        }

        override suspend fun deletePlaceBookmark(bookmarkPlaceId: Long) {
            deleteFailure?.let { throw it }
            deletedBookmarkPlaceId = bookmarkPlaceId
        }
    }
}

private fun placeBookmarkSummary(
    bookmarkPlaceId: Long,
    placeName: String = "Home",
    type: BookmarkPlaceType = BookmarkPlaceType.HOME
): PlaceBookmarkSummary {
    return PlaceBookmarkSummary(
        bookmarkPlaceId = bookmarkPlaceId,
        type = type,
        placeName = placeName,
        roadAddress = "Seoul Gangnam-gu 123",
        latitude = 37.498,
        longitude = 127.027
    )
}
