package com.example.passedpath.feature.place.presentation.viewmodel

import com.example.passedpath.feature.place.domain.model.BookmarkPlace
import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.PlaceSearchPage
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
        val place = place(id = "place-1", name = "Cafe")
        val searchRepository = FakePlaceSearchRepository(
            pagesByQuery = mapOf(
                "Cafe" to listOf(
                    searchPage(
                        page = 1,
                        isEnd = true,
                        pageableCount = 1,
                        places = listOf(place)
                    )
                )
            )
        )
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
        assertEquals(listOf("Cafe:1"), searchRepository.requestedQueries)
        assertEquals(listOf("2026-04-23"), placeRepository.addRequestDates)
        assertEquals("Cafe", placeRepository.addRequests.single().placeName)
        assertEquals("Road Address", placeRepository.addRequests.single().roadAddress)
        assertEquals(listOf(1L), creationEvents)

        eventJob.cancel()
    }

    @Test
    fun `new search resets pagination state and loads first page`() = runTest(
        context = mainDispatcherRule.dispatcher
    ) {
        val searchRepository = FakePlaceSearchRepository(
            pagesByQuery = mapOf(
                "Cafe" to listOf(
                    searchPage(
                        page = 1,
                        isEnd = false,
                        pageableCount = 2,
                        places = listOf(place(id = "place-1", name = "Cafe One"))
                    ),
                    searchPage(
                        page = 2,
                        isEnd = true,
                        pageableCount = 2,
                        places = listOf(place(id = "place-2", name = "Cafe Two"))
                    )
                ),
                "Bakery" to listOf(
                    searchPage(
                        page = 1,
                        isEnd = true,
                        pageableCount = 1,
                        places = listOf(place(id = "place-3", name = "Bakery"))
                    )
                )
            )
        )
        val viewModel = createViewModel(searchRepository)

        viewModel.onQueryChanged("Cafe")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.onLoadNextPage()
        advanceUntilIdle()
        viewModel.onQueryChanged("Bakery")
        advanceTimeBy(400)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Bakery", state.query)
        assertEquals(1, state.currentPage)
        assertTrue(state.isEnd)
        assertEquals(1, state.pageableCount)
        assertEquals(listOf("Bakery"), state.places.map { it.name })
    }

    @Test
    fun `non blank query marks awaiting first search before debounce`() = runTest(
        context = mainDispatcherRule.dispatcher
    ) {
        val viewModel = createViewModel(FakePlaceSearchRepository())

        viewModel.onQueryChanged("Cafe")

        val state = viewModel.uiState.value
        assertEquals("Cafe", state.query)
        assertTrue(state.isAwaitingFirstSearch)
        assertFalse(state.isLoading)
        assertTrue(state.places.isEmpty())
    }

    @Test
    fun `load next page appends results and stops at end`() = runTest(
        context = mainDispatcherRule.dispatcher
    ) {
        val searchRepository = FakePlaceSearchRepository(
            pagesByQuery = mapOf(
                "Cafe" to listOf(
                    searchPage(
                        page = 1,
                        size = 2,
                        isEnd = false,
                        pageableCount = 3,
                        places = listOf(
                            place(id = "place-1", name = "Cafe One"),
                            place(id = "place-2", name = "Cafe Two")
                        )
                    ),
                    searchPage(
                        page = 2,
                        size = 2,
                        isEnd = true,
                        pageableCount = 3,
                        places = listOf(
                            place(id = "place-2", name = "Cafe Two"),
                            place(id = "place-3", name = "Cafe Three")
                        )
                    )
                )
            )
        )
        val viewModel = createViewModel(searchRepository)

        viewModel.onQueryChanged("Cafe")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.onLoadNextPage()
        advanceUntilIdle()
        viewModel.onLoadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("Cafe:1", "Cafe:2"), searchRepository.requestedQueries)
        assertEquals(2, state.currentPage)
        assertTrue(state.isEnd)
        assertEquals(3, state.places.size)
        assertEquals(listOf("Cafe One", "Cafe Two", "Cafe Three"), state.places.map { it.name })
    }

    @Test
    fun `load next page failure keeps existing results and exposes error`() = runTest(
        context = mainDispatcherRule.dispatcher
    ) {
        val searchRepository = FakePlaceSearchRepository(
            pagesByQuery = mapOf(
                "Cafe" to listOf(
                    searchPage(
                        page = 1,
                        isEnd = false,
                        pageableCount = 2,
                        places = listOf(place(id = "place-1", name = "Cafe One"))
                    )
                )
            ),
            failureByQueryAndPage = mapOf("Cafe:2" to IllegalStateException("boom"))
        )
        val viewModel = createViewModel(searchRepository)

        viewModel.onQueryChanged("Cafe")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.onLoadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.places.size)
        assertEquals("Cafe One", state.places.single().name)
        assertEquals(1, state.currentPage)
        assertFalse(state.isLoadingNextPage)
        assertFalse(state.isAwaitingFirstSearch)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun `load next page ignored when first page already ended`() = runTest(
        context = mainDispatcherRule.dispatcher
    ) {
        val searchRepository = FakePlaceSearchRepository(
            pagesByQuery = mapOf(
                "Cafe" to listOf(
                    searchPage(
                        page = 1,
                        isEnd = true,
                        pageableCount = 1,
                        places = listOf(place(id = "place-1", name = "Cafe"))
                    )
                )
            )
        )
        val viewModel = createViewModel(searchRepository)

        viewModel.onQueryChanged("Cafe")
        advanceTimeBy(400)
        advanceUntilIdle()
        viewModel.onLoadNextPage()
        advanceUntilIdle()

        assertEquals(listOf("Cafe:1"), searchRepository.requestedQueries)
    }

    @Test
    fun `blank query clears awaiting first search`() = runTest(
        context = mainDispatcherRule.dispatcher
    ) {
        val viewModel = createViewModel(FakePlaceSearchRepository())

        viewModel.onQueryChanged("Cafe")
        viewModel.onQueryChanged("")

        val state = viewModel.uiState.value
        assertFalse(state.isAwaitingFirstSearch)
        assertTrue(state.places.isEmpty())
        assertEquals(0, state.currentPage)
    }

    private fun createViewModel(searchRepository: FakePlaceSearchRepository): AddPlaceViewModel {
        return AddPlaceViewModel(
            dateKey = "2026-04-23",
            searchPlacesUseCase = SearchPlacesUseCase(searchRepository),
            createPlaceFromSearchResultUseCase = CreatePlaceFromSearchResultUseCase(
                addPlaceUseCase = AddPlaceUseCase(FakePlaceRepository())
            )
        )
    }

    private fun searchPage(
        page: Int,
        size: Int = 1,
        isEnd: Boolean,
        pageableCount: Int,
        places: List<PlaceSearchResult>
    ): PlaceSearchPage {
        return PlaceSearchPage(
            page = page,
            size = size,
            isEnd = isEnd,
            pageableCount = pageableCount,
            places = places
        )
    }

    private fun place(id: String, name: String): PlaceSearchResult {
        return PlaceSearchResult(
            id = id,
            name = name,
            category = "Food",
            roadAddress = "Road Address",
            address = "Address",
            latitude = 37.1,
            longitude = 127.1
        )
    }

    private class FakePlaceSearchRepository(
        private val pagesByQuery: Map<String, List<PlaceSearchPage>> = emptyMap(),
        private val failureByQueryAndPage: Map<String, Throwable> = emptyMap()
    ) : PlaceSearchRepository {
        val requestedQueries = mutableListOf<String>()

        override suspend fun search(query: String, page: Int): PlaceSearchPage {
            requestedQueries += "$query:$page"
            failureByQueryAndPage["$query:$page"]?.let { throw it }
            return pagesByQuery[query]?.getOrNull(page - 1)
                ?: PlaceSearchPage(
                    page = page,
                    size = 0,
                    isEnd = true,
                    pageableCount = 0,
                    places = emptyList()
                )
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
