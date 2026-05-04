package com.example.passedpath.feature.placebookmark.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary
import com.example.passedpath.feature.placebookmark.domain.model.RegisteredPlaceBookmark
import com.example.passedpath.feature.placebookmark.domain.usecase.CreatePlaceBookmarkUseCase
import com.example.passedpath.feature.placebookmark.domain.usecase.GetPlaceBookmarksUseCase
import com.example.passedpath.feature.placebookmark.presentation.state.PlaceBookmarkUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaceBookmarkViewModel(
    private val getPlaceBookmarksUseCase: GetPlaceBookmarksUseCase,
    private val createPlaceBookmarkUseCase: CreatePlaceBookmarkUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlaceBookmarkUiState())
    val uiState: StateFlow<PlaceBookmarkUiState> = _uiState.asStateFlow()
    private val _placeBookmarkCreated = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val placeBookmarkCreated: SharedFlow<Long> = _placeBookmarkCreated.asSharedFlow()

    fun fetchPlaceBookmarks() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            runCatching {
                getPlaceBookmarksUseCase()
            }.onSuccess { placeBookmarkList ->
                _uiState.update {
                    it.copy(
                        placeCount = placeBookmarkList.placeCount,
                        bookmarkPlaces = placeBookmarkList.bookmarkPlaces,
                        hasLoaded = true,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        hasLoaded = it.hasLoaded,
                        isLoading = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable),
                        successMessage = null,
                        feedbackEventId = it.feedbackEventId + 1
                    )
                }
            }
        }
    }

    fun createPlaceBookmark(
        type: BookmarkPlaceType,
        placeName: String,
        roadAddress: String,
        latitude: Double,
        longitude: Double
    ) {
        val trimmedPlaceName = placeName.trim()
        val trimmedRoadAddress = roadAddress.trim()
        if (_uiState.value.isSubmitting) return

        when {
            trimmedPlaceName.isBlank() -> {
                emitValidationError("장소명을 입력해 주세요.")
                return
            }

            trimmedRoadAddress.isBlank() -> {
                emitValidationError("주소를 선택해 주세요.")
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            runCatching {
                createPlaceBookmarkUseCase(
                    type = type,
                    placeName = trimmedPlaceName,
                    roadAddress = trimmedRoadAddress,
                    latitude = latitude,
                    longitude = longitude
                )
            }.onSuccess { registeredPlaceBookmark ->
                val addedPlaceBookmark = registeredPlaceBookmark.toSummary()
                _uiState.update { state ->
                    val nextPlaces = state.bookmarkPlaces
                        .filterNot { it.bookmarkPlaceId == addedPlaceBookmark.bookmarkPlaceId } +
                        addedPlaceBookmark
                    state.copy(
                        placeCount = nextPlaces.size,
                        bookmarkPlaces = nextPlaces,
                        hasLoaded = true,
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = "즐겨찾는 장소를 추가했습니다.",
                        feedbackEventId = state.feedbackEventId + 1
                    )
                }
                _placeBookmarkCreated.emit(registeredPlaceBookmark.bookmarkPlaceId)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable),
                        successMessage = null,
                        feedbackEventId = it.feedbackEventId + 1
                    )
                }
            }
        }
    }

    fun consumeFeedback(eventId: Long) {
        _uiState.update {
            if (it.feedbackEventId == eventId) {
                it.copy(
                    errorMessage = null,
                    successMessage = null
                )
            } else {
                it
            }
        }
    }

    private fun emitValidationError(message: String) {
        _uiState.update {
            it.copy(
                errorMessage = message,
                successMessage = null,
                feedbackEventId = it.feedbackEventId + 1
            )
        }
    }
}

private fun RegisteredPlaceBookmark.toSummary(): PlaceBookmarkSummary {
    return PlaceBookmarkSummary(
        bookmarkPlaceId = bookmarkPlaceId,
        type = type,
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude
    )
}

class PlaceBookmarkViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceBookmarkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaceBookmarkViewModel(
                getPlaceBookmarksUseCase = appContainer.getPlaceBookmarksUseCase,
                createPlaceBookmarkUseCase = appContainer.createPlaceBookmarkUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
