package com.example.passedpath.feature.place.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.domain.repository.PlaceGuideRepository
import com.example.passedpath.feature.place.domain.usecase.GetVisitedPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.ReorderPlacesUseCase
import com.example.passedpath.feature.place.presentation.state.PlaceListUiState
import com.example.passedpath.feature.place.presentation.state.PlaceUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

class PlaceViewModel(
    private val reorderPlacesUseCase: ReorderPlacesUseCase,
    private val getVisitedPlacesUseCase: GetVisitedPlacesUseCase,
    private val placeGuideRepository: PlaceGuideRepository = InMemoryPlaceGuideRepository(),
    initialDateKey: String = todayDateKey()
) : ViewModel() {
    private var isReorderGuideBannerDismissed = false
    private var hasRequestedReorderGuideBannerDismissal = false

    private val _uiState = MutableStateFlow(
        PlaceUiState(
            dateKey = initialDateKey,
            placeList = PlaceListUiState(dateKey = initialDateKey)
        )
    )
    val uiState: StateFlow<PlaceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            placeGuideRepository.isReorderGuideBannerDismissed.collect { dismissed ->
                isReorderGuideBannerDismissed = dismissed || hasRequestedReorderGuideBannerDismissal
                _uiState.update { state ->
                    state.copy(
                        placeList = state.placeList.withReorderGuideBannerVisibility(isReorderGuideBannerDismissed)
                    )
                }
            }
        }
    }

    // 날짜를 바꾸고, 이전 날짜의 목록 상태는 지운다.
    fun updateDateKey(value: String) {
        updateField {
            copy(
                dateKey = value,
                placeList = if (placeList.dateKey == value) {
                    placeList.copy(errorMessage = null, isStale = false)
                } else {
                    PlaceListUiState(dateKey = value)
                },
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // 현재 날짜의 장소 목록을 다시 불러온다.
    fun fetchVisitedPlaces(dateKey: String = _uiState.value.dateKey) {
        if (!isValidDateKey(dateKey)) {
            _uiState.update {
                it.copy(
                    placeList = it.placeList.copy(
                        dateKey = dateKey,
                        hasLoaded = false,
                        isLoading = false,
                        errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다."
                    ),
                    errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다.",
                    successMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            refreshVisitedPlaces(dateKey = dateKey, clearFeedbackMessage = true)
        }
    }

    fun reorderPlaces(placeIds: List<Long>) {
        val currentState = _uiState.value
        if (currentState.isSubmitting) return

        if (!isValidDateKey(currentState.dateKey)) {
            _uiState.update {
                it.copy(
                    errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다.",
                    successMessage = null
                )
            }
            return
        }

        val currentPlaceIds = currentState.placeList.places
            .sortedBy(VisitedPlace::orderIndex)
            .map(VisitedPlace::placeId)

        if (placeIds == currentPlaceIds) return

        if (!hasSamePlaceIds(currentPlaceIds, placeIds)) {
            _uiState.update {
                it.copy(
                    errorMessage = "현재 장소 목록과 순서 변경 요청이 일치하지 않습니다.",
                    successMessage = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSubmitting = true,
                errorMessage = null,
                successMessage = null
            )
        }

        viewModelScope.launch {
            try {
                reorderPlacesUseCase(
                    dateKey = currentState.dateKey,
                    placeIds = placeIds
                )
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = "장소 순서가 변경되었습니다.",
                        placeList = it.placeList.copy(
                            places = it.placeList.places.reorderedBy(placeIds)
                        )
                    )
                }
                refreshVisitedPlaces(dateKey = currentState.dateKey)
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable),
                        successMessage = null
                    )
                }
            }
        }
    }

    fun dismissReorderGuideBanner() {
        if (isReorderGuideBannerDismissed) return

        hasRequestedReorderGuideBannerDismissal = true
        isReorderGuideBannerDismissed = true
        _uiState.update { state ->
            state.copy(
                placeList = state.placeList.copy(isReorderGuideBannerVisible = false)
            )
        }

        viewModelScope.launch {
            placeGuideRepository.dismissReorderGuideBanner()
        }
    }

    // 목록 재조회 로직을 한 곳에서 처리한다.
    private suspend fun refreshVisitedPlaces(
        dateKey: String,
        clearFeedbackMessage: Boolean = false
    ) {
        _uiState.update {
            val currentPlaceList = it.placeList
            it.copy(
                dateKey = dateKey,
                placeList = currentPlaceList.copy(
                    dateKey = dateKey,
                    isLoading = true,
                    errorMessage = null,
                    isStale = false
                ),
                errorMessage = null,
                successMessage = if (clearFeedbackMessage) null else it.successMessage
            )
        }

        try {
            val result = getVisitedPlacesUseCase(dateKey)
            _uiState.update {
                it.copy(
                    dateKey = dateKey,
                    placeList = PlaceListUiState(
                        dateKey = dateKey,
                        places = result.places,
                        placeCount = result.placeCount,
                        hasLoaded = true,
                        isLoading = false,
                        errorMessage = null,
                        isStale = false
                    ).withReorderGuideBannerVisibility(isReorderGuideBannerDismissed),
                    errorMessage = null,
                    successMessage = if (clearFeedbackMessage) null else it.successMessage
                )
            }
        } catch (throwable: Throwable) {
            val errorMessage = ApiFailureMessage.fromThrowable(throwable)
            _uiState.update {
                val currentPlaceList = it.placeList
                val hasRetainedContent = currentPlaceList.hasRetainedContent
                it.copy(
                    dateKey = dateKey,
                    placeList = currentPlaceList.copy(
                        dateKey = dateKey,
                        isLoading = false,
                        errorMessage = errorMessage,
                        isStale = hasRetainedContent
                    ),
                    errorMessage = errorMessage,
                    successMessage = if (clearFeedbackMessage) null else it.successMessage
                )
            }
        }
    }

    // 입력 필드 상태를 공통 방식으로 바꾼다.
    private inline fun updateField(transform: PlaceUiState.() -> PlaceUiState) {
        _uiState.update { currentState -> currentState.transform() }
    }

    private fun hasSamePlaceIds(currentPlaceIds: List<Long>, requestedPlaceIds: List<Long>): Boolean {
        return currentPlaceIds.size == requestedPlaceIds.size &&
            currentPlaceIds.toSet() == requestedPlaceIds.toSet()
    }

    private fun List<VisitedPlace>.reorderedBy(placeIds: List<Long>): List<VisitedPlace> {
        val placeById = associateBy(VisitedPlace::placeId)
        return placeIds.mapIndexedNotNull { index, placeId ->
            placeById[placeId]?.copy(orderIndex = index + 1)
        }
    }

    private fun PlaceListUiState.withReorderGuideBannerVisibility(
        isDismissed: Boolean
    ): PlaceListUiState {
        return copy(
            isReorderGuideBannerVisible = places.size >= ReorderGuideMinimumPlaceCount && !isDismissed
        )
    }

    // 날짜 형식이 맞는지 확인한다.
    private fun isValidDateKey(value: String): Boolean {
        return try {
            LocalDate.parse(value)
            true
        } catch (_: DateTimeParseException) {
            false
        }
    }
}

private const val ReorderGuideMinimumPlaceCount = 2

private class InMemoryPlaceGuideRepository : PlaceGuideRepository {
    private val dismissed = MutableStateFlow(false)

    override val isReorderGuideBannerDismissed: Flow<Boolean> = dismissed

    override suspend fun dismissReorderGuideBanner() {
        dismissed.value = true
    }
}

// 기본 날짜값으로 오늘 날짜를 만든다.
private fun todayDateKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
}

// AppContainer로 PlaceViewModel을 만든다.
class PlaceViewModelFactory(
    private val appContainer: AppContainer,
    private val initialDateKey: String = todayDateKey()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaceViewModel(
                reorderPlacesUseCase = appContainer.reorderPlacesUseCase,
                getVisitedPlacesUseCase = appContainer.getVisitedPlacesUseCase,
                placeGuideRepository = appContainer.placeGuideRepository,
                initialDateKey = initialDateKey
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
