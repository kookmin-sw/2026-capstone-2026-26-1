package com.example.passedpath.feature.place.presentation.viewmodel

import com.example.passedpath.feature.place.domain.model.BookmarkPlace
import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.domain.model.RegisteredPlace
import com.example.passedpath.feature.place.domain.model.UpdatedPlace
import com.example.passedpath.feature.place.domain.model.VisitedPlaceList
import com.example.passedpath.feature.place.domain.repository.PlaceRepository
import com.example.passedpath.feature.place.domain.repository.PlaceSearchRepository
import com.example.passedpath.feature.place.domain.usecase.AddPlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.CreatePlaceFromSearchResultUseCase
import com.example.passedpath.feature.place.domain.usecase.SearchPlacesUseCase
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddPlaceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `search result selection creates place for date and emits success`() = runTest(
        context = mainDispatcherRule.dispatcher
    ) {
        val place = PlaceSearchResult(
            id = "place-1",
            name = "Cafe",
            category = "Food",
            roadAddress = "Road Address",
            address = "Address",
            latitude = 37.1,
            longitude = 127.1
        )
        val searchRepository = FakePlaceSearchRepository(results = listOf(place))
        val placeRepository = FakePlaceRepository()
        val viewModel = AddPlaceViewModel(
            dateKey = "2026-04-23",
            searchPlacesUseCase = SearchPlacesUseCase(searchRepository),
            createPlaceFromSearchResultUseCase = CreatePlaceFromSearchResultUseCase(
                addPlaceUseCase = AddPlaceUseCase(placeRepository)
            )
        )
        val creationEvents = mutableListOf<Long>()
        val eventJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.placeCreated.toList(creationEvents)
        }

        viewModel.onQueryChanged("Cafe")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.onPlaceSelected(place.stableKey)

        assertTrue(viewModel.uiState.value.canConfirmPlace)

        viewModel.onAddPlaceClicked()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals(listOf("Cafe:1:10"), searchRepository.requestedQueries)
        assertEquals(listOf("2026-04-23"), placeRepository.addRequestDates)
        assertEquals("Cafe", placeRepository.addRequests.single().placeName)
        assertEquals("Road Address", placeRepository.addRequests.single().roadAddress)
        assertEquals(listOf(1L), creationEvents)

        eventJob.cancel()
    }

    private class FakePlaceSearchRepository(
        private val results: List<PlaceSearchResult>
    ) : PlaceSearchRepository {
        val requestedQueries = mutableListOf<String>()

        override suspend fun search(query: String, page: Int, size: Int): List<PlaceSearchResult> {
            requestedQueries += "$query:$page:$size"
            return results
        }
    }

    private class FakePlaceRepository : PlaceRepository {
        val addRequestDates = mutableListOf<String>()
        val addRequests = mutableListOf<PlaceRegistration>()

        override suspend fun getPlaces(dateKey: String): VisitedPlaceList {
            return VisitedPlaceList(placeCount = 0, places = emptyList())
        }

        override suspend fun addPlace(dateKey: String, place: PlaceRegistration): RegisteredPlace {
            addRequestDates += dateKey
            addRequests += place
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
            error("Not needed in test")
        }

        override suspend fun reorderPlaces(dateKey: String, placeIds: List<Long>) {
            error("Not needed in test")
        }

        override suspend fun updateBookmarkPlace(
            bookmarkPlaceId: Long,
            bookmarkPlace: BookmarkPlace
        ): BookmarkPlace {
            error("Not needed in test")
        }

        override suspend fun deletePlace(dateKey: String, placeId: Long) {
            error("Not needed in test")
        }
    }
}
