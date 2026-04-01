package com.example.passedpath.feature.place.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.place.domain.usecase.AddPlaceUseCase
import com.example.passedpath.feature.place.presentation.state.PlaceUiState
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
    private val addPlaceUseCase: AddPlaceUseCase,
    initialDateKey: String = todayDateKey()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceUiState(dateKey = initialDateKey))
    val uiState: StateFlow<PlaceUiState> = _uiState.asStateFlow()

    fun updateDateKey(value: String) {
        updateField { copy(dateKey = value, errorMessage = null, successMessage = null) }
    }

    fun updatePlaceName(value: String) {
        updateField { copy(placeName = value, errorMessage = null, successMessage = null) }
    }

    fun updateRoadAddress(value: String) {
        updateField { copy(roadAddress = value, errorMessage = null, successMessage = null) }
    }

    fun updateLatitude(value: String) {
        updateField { copy(latitude = value, errorMessage = null, successMessage = null) }
    }

    fun updateLongitude(value: String) {
        updateField { copy(longitude = value, errorMessage = null, successMessage = null) }
    }

    fun submit() {
        val currentState = _uiState.value
        val latitude = currentState.latitude.toDoubleOrNull()
        val longitude = currentState.longitude.toDoubleOrNull()

        if (!isValidDateKey(currentState.dateKey)) {
            _uiState.update { it.copy(errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다.", successMessage = null) }
            return
        }
        if (latitude == null || longitude == null) {
            _uiState.update { it.copy(errorMessage = "위도와 경도는 숫자로 입력해야 합니다.", successMessage = null) }
            return
        }
        if (!currentState.isSubmitEnabled) {
            _uiState.update { it.copy(errorMessage = "모든 필드를 입력해 주세요.", successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                val result = addPlaceUseCase(
                    dateKey = currentState.dateKey,
                    placeName = currentState.placeName,
                    roadAddress = currentState.roadAddress,
                    latitude = latitude,
                    longitude = longitude
                )
                _uiState.update {
                    it.copy(
                        placeName = "",
                        roadAddress = "",
                        latitude = "",
                        longitude = "",
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = "장소가 등록되었습니다. 순서 ${result.orderIndex}번"
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "장소 등록에 실패했습니다.",
                        successMessage = null
                    )
                }
            }
        }
    }

    private inline fun updateField(transform: PlaceUiState.() -> PlaceUiState) {
        _uiState.update { currentState -> currentState.transform() }
    }

    private fun isValidDateKey(value: String): Boolean {
        return try {
            LocalDate.parse(value)
            true
        } catch (_: DateTimeParseException) {
            false
        }
    }
}

private fun todayDateKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
}

class PlaceViewModelFactory(
    private val appContainer: AppContainer,
    private val initialDateKey: String = todayDateKey()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaceViewModel(
                addPlaceUseCase = appContainer.addPlaceUseCase,
                initialDateKey = initialDateKey
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
