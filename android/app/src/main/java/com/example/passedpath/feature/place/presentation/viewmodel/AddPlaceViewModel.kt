package com.example.passedpath.feature.place.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.place.domain.model.PlaceSearchPage
import com.example.passedpath.feature.place.domain.usecase.CreatePlaceFromSearchResultUseCase
import com.example.passedpath.feature.place.domain.usecase.SearchPlacesUseCase
import com.example.passedpath.feature.place.presentation.state.AddPlaceUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddPlaceViewModel(
    private val dateKey: String,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val createPlaceFromSearchResultUseCase: CreatePlaceFromSearchResultUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddPlaceUiState())
    val uiState: StateFlow<AddPlaceUiState> = _uiState.asStateFlow()

    private val _placeCreated = MutableSharedFlow<Long>()
    val placeCreated: SharedFlow<Long> = _placeCreated.asSharedFlow()

    private var searchJob: Job? = null
    private var lastRequestedQuery: String? = null

    fun onQueryChanged(query: String) {
        val normalizedQuery = query.trim()
        _uiState.update {
            it.copy(
                query = query,
                selectedPlaceId = null,
                errorMessage = null,
                places = emptyList(),
                isLoading = false,
                isAwaitingFirstSearch = normalizedQuery.isNotBlank(),
                isLoadingNextPage = false,
                currentPage = 0,
                isEnd = false,
                pageableCount = 0
            )
        }

        searchJob?.cancel()
        if (normalizedQuery.isBlank()) {
            lastRequestedQuery = null
            _uiState.update {
                it.copy(
                    places = emptyList(),
                    isAwaitingFirstSearch = false,
                    currentPage = 0,
                    isEnd = false,
                    pageableCount = 0
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            if (lastRequestedQuery == normalizedQuery) return@launch
            searchFirstPage(normalizedQuery)
        }
    }

    fun onLoadNextPage() {
        val state = _uiState.value
        val normalizedQuery = state.query.trim()
        if (normalizedQuery.isBlank()) return
        if (state.isLoading || state.isLoadingNextPage) return
        if (state.isEnd) return
        if (state.places.isEmpty()) return

        viewModelScope.launch {
            loadNextPage(normalizedQuery, state.currentPage + 1)
        }
    }

    fun onPlaceSelected(placeId: String) {
        _uiState.update { state ->
            if (state.places.none { it.stableKey == placeId }) {
                state
            } else {
                state.copy(selectedPlaceId = placeId)
            }
        }
    }

    fun onAddPlaceClicked() {
        val selectedPlace = _uiState.value.selectedPlace ?: return
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null
                )
            }

            try {
                val registeredPlace = createPlaceFromSearchResultUseCase(
                    dateKey = dateKey,
                    place = selectedPlace
                )
                _uiState.update { it.copy(isSubmitting = false) }
                _placeCreated.emit(registeredPlace.placeId)
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable)
                    )
                }
            }
        }
    }

    private suspend fun searchFirstPage(query: String) {
        lastRequestedQuery = query
        _uiState.update {
            it.copy(
                isLoading = true,
                isAwaitingFirstSearch = false,
                isLoadingNextPage = false,
                errorMessage = null,
                selectedPlaceId = null,
                places = emptyList(),
                currentPage = 0,
                isEnd = false,
                pageableCount = 0
            )
        }

        try {
            val resultPage = searchPlacesUseCase(query = query, page = 1)
            _uiState.update { state ->
                if (state.query.trim() != query) {
                    state
                } else {
                    state.withFirstPage(
                        resultPage = resultPage,
                        errorMessage = null
                    )
                }
            }
        } catch (throwable: Throwable) {
            _uiState.update { state ->
                if (state.query.trim() != query) {
                    state
                } else {
                    state.copy(
                        isLoading = false,
                        isAwaitingFirstSearch = false,
                        places = emptyList(),
                        currentPage = 0,
                        isEnd = false,
                        pageableCount = 0,
                        selectedPlaceId = null,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable)
                    )
                }
            }
        }
    }

    private suspend fun loadNextPage(query: String, page: Int) {
        _uiState.update { state ->
            if (state.query.trim() != query) {
                state
            } else {
                state.copy(
                    isLoadingNextPage = true,
                    errorMessage = null
                )
            }
        }

        try {
            val resultPage = searchPlacesUseCase(query = query, page = page)
            _uiState.update { state ->
                if (state.query.trim() != query) {
                    state
                } else {
                    val existingKeys = state.places.mapTo(linkedSetOf()) { it.stableKey }
                    val nextPlaces = resultPage.places.filter { existingKeys.add(it.stableKey) }
                    state.copy(
                        isLoadingNextPage = false,
                        places = state.places + nextPlaces,
                        currentPage = resultPage.page,
                        isEnd = resultPage.isEnd,
                        pageableCount = resultPage.pageableCount,
                        errorMessage = null
                    )
                }
            }
        } catch (throwable: Throwable) {
            _uiState.update { state ->
                if (state.query.trim() != query) {
                    state
                } else {
                    state.copy(
                        isLoadingNextPage = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable)
                    )
                }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 400L
    }
}

private fun AddPlaceUiState.withFirstPage(
    resultPage: PlaceSearchPage,
    errorMessage: String?
): AddPlaceUiState {
    return copy(
        isLoading = false,
        isAwaitingFirstSearch = false,
        places = resultPage.places,
        currentPage = resultPage.page,
        isEnd = resultPage.isEnd,
        pageableCount = resultPage.pageableCount,
        selectedPlaceId = null,
        errorMessage = errorMessage
    )
}

class AddPlaceViewModelFactory(
    private val appContainer: AppContainer,
    private val dateKey: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPlaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddPlaceViewModel(
                dateKey = dateKey,
                searchPlacesUseCase = appContainer.searchPlacesUseCase,
                createPlaceFromSearchResultUseCase = appContainer.createPlaceFromSearchResultUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
