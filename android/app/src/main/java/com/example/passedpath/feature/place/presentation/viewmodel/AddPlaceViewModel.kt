package com.example.passedpath.feature.place.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
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

    private val _placeCreated = MutableSharedFlow<Unit>()
    val placeCreated: SharedFlow<Unit> = _placeCreated.asSharedFlow()

    private var searchJob: Job? = null
    private var lastRequestedQuery: String? = null

    fun onQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                selectedPlaceId = null,
                errorMessage = null,
                places = if (query.isBlank()) emptyList() else it.places,
                isLoading = if (query.isBlank()) false else it.isLoading
            )
        }

        searchJob?.cancel()
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            lastRequestedQuery = null
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            if (lastRequestedQuery == normalizedQuery) return@launch
            searchPlaces(normalizedQuery)
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
                createPlaceFromSearchResultUseCase(
                    dateKey = dateKey,
                    place = selectedPlace
                )
                _uiState.update { it.copy(isSubmitting = false) }
                _placeCreated.emit(Unit)
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

    private suspend fun searchPlaces(query: String) {
        lastRequestedQuery = query
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                selectedPlaceId = null
            )
        }

        try {
            val results = searchPlacesUseCase(query)
            _uiState.update { state ->
                if (state.query.trim() != query) {
                    state
                } else {
                    state.copy(
                        isLoading = false,
                        places = results,
                        selectedPlaceId = null,
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
                        places = emptyList(),
                        selectedPlaceId = null,
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
