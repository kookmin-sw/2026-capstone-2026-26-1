package com.example.passedpath.feature.place.presentation.viewmodel

import com.example.passedpath.feature.place.domain.model.BookmarkPlace
import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.PlaceSourceType
import com.example.passedpath.feature.place.domain.model.RegisteredPlace
import com.example.passedpath.feature.place.domain.model.UpdatedPlace
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.domain.model.VisitedPlaceList
import com.example.passedpath.feature.place.domain.repository.PlaceRepository
import com.example.passedpath.feature.place.domain.usecase.AddPlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.DeletePlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.GetVisitedPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.ReorderPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.UpdatePlaceUseCase
import com.example.passedpath.testutil.MainDispatcherRule
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
                        VisitedPlace(
                            placeId = 2L,
                            placeName = "Cafe",
                            type = PlaceSourceType.MANUAL,
                            roadAddress = "Seoul Forest 2-gil",
                            latitude = 37.5,
                            longitude = 127.5,
                            orderIndex = 2
                        ),
                        VisitedPlace(
                            placeId = 1L,
                            placeName = "Seoul Forest",
                            type = PlaceSourceType.AUTO,
                            roadAddress = "Ttukseom-ro",
                            latitude = 37.4,
                            longitude = 127.4,
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
        assertEquals("boom", state.placeList.errorMessage)
        assertEquals("boom", state.errorMessage)
    }

    @Test
    fun `fetchVisitedPlaces rejects invalid date before repository call`() = runTest {
        val repository = FakePlaceRepository()
        val viewModel = createViewModel(repository = repository, initialDateKey = "2026-04-03")

        viewModel.fetchVisitedPlaces("invalid-date")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.placeList.isLoading)
        assertEquals("날짜는 yyyy-MM-dd 형식이어야 합니다.", state.placeList.errorMessage)
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

    private fun createViewModel(
        repository: FakePlaceRepository,
        initialDateKey: String
    ): PlaceViewModel {
        return PlaceViewModel(
            addPlaceUseCase = AddPlaceUseCase(repository),
            updatePlaceUseCase = UpdatePlaceUseCase(repository),
            deletePlaceUseCase = DeletePlaceUseCase(repository),
            reorderPlacesUseCase = ReorderPlacesUseCase(repository),
            getVisitedPlacesUseCase = GetVisitedPlacesUseCase(repository),
            initialDateKey = initialDateKey
        )
    }

    private class FakePlaceRepository(
        private val visitedPlaceListByDate: MutableMap<String, VisitedPlaceList> = mutableMapOf(),
        private val throwOnGetPlaces: Throwable? = null
    ) : PlaceRepository {
        val requestedPlaceListDates = mutableListOf<String>()

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

        override suspend fun reorderPlaces(dateKey: String, placeIds: List<Long>) = Unit

        override suspend fun updateBookmarkPlace(
            bookmarkPlaceId: Long,
            bookmarkPlace: BookmarkPlace
        ): BookmarkPlace {
            return bookmarkPlace
        }

        override suspend fun deletePlace(dateKey: String, placeId: Long) = Unit
    }
}
