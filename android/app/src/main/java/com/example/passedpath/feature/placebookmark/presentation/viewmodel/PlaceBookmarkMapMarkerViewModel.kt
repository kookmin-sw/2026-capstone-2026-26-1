package com.example.passedpath.feature.placebookmark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.placebookmark.domain.usecase.GetPlaceBookmarksUseCase
import com.example.passedpath.feature.placebookmark.presentation.state.PlaceBookmarkMapMarkerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaceBookmarkMapMarkerViewModel(
    private val getPlaceBookmarksUseCase: GetPlaceBookmarksUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlaceBookmarkMapMarkerUiState())
    val uiState: StateFlow<PlaceBookmarkMapMarkerUiState> = _uiState.asStateFlow()

    fun fetchPlaceBookmarkMarkers() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            runCatching {
                getPlaceBookmarksUseCase()
            }.onSuccess { placeBookmarkList ->
                _uiState.update {
                    it.copy(
                        bookmarkPlaces = placeBookmarkList.bookmarkPlaces,
                        hasLoaded = true,
                        isLoading = false
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}

class PlaceBookmarkMapMarkerViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceBookmarkMapMarkerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaceBookmarkMapMarkerViewModel(
                getPlaceBookmarksUseCase = appContainer.getPlaceBookmarksUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
