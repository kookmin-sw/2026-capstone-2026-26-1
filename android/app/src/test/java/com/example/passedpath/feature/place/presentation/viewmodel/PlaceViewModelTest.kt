package com.example.passedpath.feature.place.presentation.viewmodel

import com.example.passedpath.feature.place.domain.model.BookmarkPlace
import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.PlaceSourceType
import com.example.passedpath.feature.place.domain.model.RegisteredPlace
import com.example.passedpath.feature.place.domain.model.UpdatedPlace
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.domain.model.VisitedPlaceList
import com.example.passedpath.feature.place.domain.repository.PlaceRepository
import com.example.passedpath.feature.place.domain.usecase.GetVisitedPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.ReorderPlacesUseCase
import com.example.passedpath.testutil.MainDispatcherRule
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `fetchVisitedPlaces loads visited place list into ui state`() = runTest {
        val repository = FakePlaceRepository(
            visitedPlaceListByDate = mutableMapOf(
                "2026-04-03" to VisitedPlaceList(
                    placeCount = 2,
                    places = listOf(
                        visitedPlace(
                            placeId = 2L,
                            placeName = "Cafe",
                            source = PlaceSourceType.MANUAL,
                            orderIndex = 2
                        ),
                        visitedPlace(
                            placeId = 1L,
                            placeName = "Seoul Forest",
                            source = PlaceSourceType.AUTO,
                            orderIndex = 1
                        )
                    )
                )
            )
        )
        val viewModel = createViewModel(repository = repository, initialDateKey = "2026-04-03")

        viewModel.fetchVisitedPlaces()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("2026-04-03", state.placeList.dateKey)
        assertFalse(state.placeList.isLoading)
        assertNull(state.placeList.errorMessage)
        assertEquals(2, state.placeList.placeCount)
        assertEquals(2, state.placeList.places.size)
        assertEquals(listOf("2026-04-03"), repository.requestedPlaceListDates)
    }

    @Test
    fun `fetchVisitedPlaces exposes repository failure in place list state`() = runTest {
        val repository = FakePlaceRepository(
            throwOnGetPlaces = IllegalStateException("boom")
        )
        val viewModel = createViewModel(repository = repository, initialDateKey = "2026-04-03")

        viewModel.fetchVisitedPlaces()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.placeList.isLoading)
        assertEquals(ApiFailureMessage.NETWORK_REQUEST_FAILED, state.placeList.errorMessage)
        assertEquals(ApiFailureMessage.NETWORK_REQUEST_FAILED, state.errorMessage)
    }

    @Test
    fun `fetchVisitedPlaces keeps stale content on failure after a successful load`() = runTest {
        val repository = FakePlaceRepository(
            visitedPlaceListByDate = mutableMapOf(
                "2026-04-03" to VisitedPlaceList(
                    placeCount = 1,
                    places = listOf(visitedPlace(placeId = 1L, placeName = "Seoul Forest"))
                )
            )
        )
        val viewModel = createViewModel(repository = repository, initialDateKey = "2026-04-03")

        viewModel.fetchVisitedPlaces()
        advanceUntilIdle()

        repository.throwOnGetPlaces = IllegalStateException("boom")
        viewModel.fetchVisitedPlaces()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.placeList.places.size)
        assertEquals(1, state.placeList.placeCount)
        assertEquals("Seoul Forest", state.placeList.places.first().placeName)
        assertTrue(state.placeList.isStale)
        assertEquals(ApiFailureMessage.NETWORK_REQUEST_FAILED, state.placeList.errorMessage)
    }

    @Test
    fun `retry after stale failure restores fresh place list state`() = runTest {
        val repository = FakePlaceRepository(
            visitedPlaceListByDate = mutableMapOf(
                "2026-04-03" to VisitedPlaceList(
                    placeCount = 1,
                    places = listOf(visitedPlace(placeId = 1L, placeName = "Seoul Forest"))
                )
            )
        )
        val viewModel = createViewModel(repository = repository, initialDateKey = "2026-04-03")

        viewModel.fetchVisitedPlaces()
        advanceUntilIdle()

        repository.throwOnGetPlaces = IllegalStateException("boom")
        viewModel.fetchVisitedPlaces()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.placeList.isStale)

        repository.throwOnGetPlaces = null
        repository.visitedPlaceListByDate["2026-04-03"] = VisitedPlaceList(
            placeCount = 2,
            places = listOf(
                visitedPlace(placeId = 1L, placeName = "Seoul Forest"),
                visitedPlace(
                    placeId = 2L,
                    placeName = "Cafe",
                    source = PlaceSourceType.MANUAL,
                    orderIndex = 2
                )
            )
        )

        viewModel.fetchVisitedPlaces()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.placeList.isStale)
        assertNull(state.placeList.errorMessage)
        assertEquals(2, state.placeList.placeCount)
        assertEquals(listOf("Seoul Forest", "Cafe"), state.placeList.places.map { it.placeName })
    }

    @Test
    fun `fetchVisitedPlaces rejects invalid date before repository call`() = runTest {
        val repository = FakePlaceRepository()
        val viewModel = createViewModel(repository = repository, initialDateKey = "2026-04-03")

        viewModel.fetchVisitedPlaces("invalid-date")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.placeList.isLoading)
        assertEquals(
            "\ub0a0\uc9dc\ub294 yyyy-MM-dd \ud615\uc2dd\uc774\uc5b4\uc57c \ud569\ub2c8\ub2e4.",
            state.placeList.errorMessage
        )
        assertTrue(repository.requestedPlaceListDates.isEmpty())
    }

    @Test
    fun `updateDateKey resets place list state for a new date`() {
        val repository = FakePlaceRepository()
        val viewModel = createViewModel(repository = repository, initialDateKey = "2026-04-03")

        viewModel.updateDateKey("2026-04-04")

        val state = viewModel.uiState.value
        assertEquals("2026-04-04", state.dateKey)
        assertEquals("2026-04-04", state.placeList.dateKey)
        assertTrue(state.placeList.places.isEmpty())
        assertEquals(0, state.placeList.placeCount)
        assertNull(state.placeList.errorMessage)
    }

    @Test
    fun `reorderPlaces refreshes place list after success`() = runTest {
        val repository = FakePlaceRepository()
        val viewModel = createViewModel(repository = repository, initialDateKey = "2026-04-03")

        viewModel.updateReorderPlaceIdsInput("3,1,2")

        viewModel.reorderPlaces()
        advanceUntilIdle()

        assertEquals(listOf("2026-04-03"), repository.requestedPlaceListDates)
        assertEquals(listOf(listOf(3L, 1L, 2L)), repository.reorderRequests)
        assertEquals(
            "\uc7a5\uc18c \uc21c\uc11c\uac00 \ubcc0\uacbd\ub418\uc5c8\uc2b5\ub2c8\ub2e4. ids=3,1,2",
            viewModel.uiState.value.successMessage
        )
    }

    private fun createViewModel(
        repository: FakePlaceRepository,
        initialDateKey: String
    ): PlaceViewModel {
        return PlaceViewModel(
            reorderPlacesUseCase = ReorderPlacesUseCase(repository),
            getVisitedPlacesUseCase = GetVisitedPlacesUseCase(repository),
            initialDateKey = initialDateKey
        )
    }

    private fun visitedPlace(
        placeId: Long,
        placeName: String,
        source: PlaceSourceType = PlaceSourceType.AUTO,
        orderIndex: Int = 1
    ): VisitedPlace {
        return VisitedPlace(
            placeId = placeId,
            placeName = placeName,
            source = source,
            roadAddress = "Ttukseom-ro",
            latitude = 37.4,
            longitude = 127.4,
            orderIndex = orderIndex
        )
    }

    private class FakePlaceRepository(
        val visitedPlaceListByDate: MutableMap<String, VisitedPlaceList> = mutableMapOf(),
        var throwOnGetPlaces: Throwable? = null
    ) : PlaceRepository {
        val requestedPlaceListDates = mutableListOf<String>()
        val reorderRequests = mutableListOf<List<Long>>()

        override suspend fun getPlaces(dateKey: String): VisitedPlaceList {
            requestedPlaceListDates += dateKey
            throwOnGetPlaces?.let { throw it }
            return visitedPlaceListByDate[dateKey] ?: VisitedPlaceList(
                placeCount = 0,
                places = emptyList()
            )
        }

        override suspend fun addPlace(dateKey: String, place: PlaceRegistration): RegisteredPlace {
            return RegisteredPlace(
                placeId = 1L,
                placeName = place.placeName,
                roadAddress = place.roadAddress,
                latitude = place.latitude,
                longitude = place.longitude,
                orderIndex = 1
            )
        }

        override suspend fun updatePlace(
            dateKey: String,
            placeId: Long,
            place: PlaceRegistration
        ): UpdatedPlace {
            return UpdatedPlace(
                placeName = place.placeName,
                roadAddress = place.roadAddress,
                latitude = place.latitude,
                longitude = place.longitude
            )
        }

        override suspend fun reorderPlaces(dateKey: String, placeIds: List<Long>) {
            reorderRequests += placeIds
        }

        override suspend fun updateBookmarkPlace(
            bookmarkPlaceId: Long,
            bookmarkPlace: BookmarkPlace
        ): BookmarkPlace {
            return bookmarkPlace
        }

        override suspend fun deletePlace(dateKey: String, placeId: Long) = Unit
    }
}
