package com.example.passedpath.feature.place.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.place.domain.usecase.AddPlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.DeletePlaceUseCase
import com.example.passedpath.feature.place.domain.usecase.ReorderPlacesUseCase
import com.example.passedpath.feature.place.domain.usecase.UpdatePlaceUseCase
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
    private val updatePlaceUseCase: UpdatePlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val reorderPlacesUseCase: ReorderPlacesUseCase,
    initialDateKey: String = todayDateKey()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceUiState(dateKey = initialDateKey))
    val uiState: StateFlow<PlaceUiState> = _uiState.asStateFlow()

    fun updateDateKey(value: String) {
        updateField { copy(dateKey = value, errorMessage = null, successMessage = null) }
    }

    fun updatePlaceId(value: String) {
        updateField { copy(placeId = value, errorMessage = null, successMessage = null) }
    }

    fun updateReorderPlaceIdsInput(value: String) {
        updateField { copy(reorderPlaceIdsInput = value, errorMessage = null, successMessage = null) }
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
        val placeId = currentState.parsedPlaceId

        val validationError = validateForMutation(
            state = currentState,
            latitude = latitude,
            longitude = longitude
        )
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError, successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                if (placeId == null) {
                    val result = addPlaceUseCase(
                        dateKey = currentState.dateKey,
                        placeName = currentState.placeName,
                        roadAddress = currentState.roadAddress,
                        latitude = latitude!!,
                        longitude = longitude!!
                    )
                    _uiState.update {
                        it.copy(
                            placeId = result.placeId.toString(),
                            isSubmitting = false,
                            errorMessage = null,
                            successMessage = "장소가 등록되었습니다. placeId=${result.placeId}, 순서 ${result.orderIndex}번"
                        )
                    }
                } else {
                    updatePlaceUseCase(
                        dateKey = currentState.dateKey,
                        placeId = placeId,
                        placeName = currentState.placeName,
                        roadAddress = currentState.roadAddress,
                        latitude = latitude!!,
                        longitude = longitude!!
                    )
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = null,
                            successMessage = "장소가 수정되었습니다. placeId=$placeId"
                        )
                    }
                }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: mutationFailureMessage(placeId == null),
                        successMessage = null
                    )
                }
            }
        }
    }

    fun reorderPlaces() {
        val currentState = _uiState.value
        val placeIds = currentState.parsedReorderPlaceIds
        if (!isValidDateKey(currentState.dateKey)) {
            _uiState.update { it.copy(errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다.", successMessage = null) }
            return
        }
        if (placeIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "순서 변경용 placeId 배열을 입력해 주세요.", successMessage = null) }
            return
        }
        if (!isValidReorderInput(currentState.reorderPlaceIdsInput, placeIds)) {
            _uiState.update { it.copy(errorMessage = "순서 변경 입력은 쉼표로 구분된 숫자 목록이어야 합니다. 예: 2,1", successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                reorderPlacesUseCase(
                    dateKey = currentState.dateKey,
                    placeIds = placeIds
                )
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = "장소 순서가 변경되었습니다. ids=${placeIds.joinToString(",")}" 
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "장소 순서 변경에 실패했습니다.",
                        successMessage = null
                    )
                }
            }
        }
    }

    fun deletePlace() {
        val currentState = _uiState.value
        val placeId = currentState.parsedPlaceId

        if (!isValidDateKey(currentState.dateKey)) {
            _uiState.update { it.copy(errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다.", successMessage = null) }
            return
        }
        if (placeId == null) {
            _uiState.update { it.copy(errorMessage = "삭제할 placeId를 입력해 주세요.", successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                deletePlaceUseCase(
                    dateKey = currentState.dateKey,
                    placeId = placeId
                )
                _uiState.update {
                    it.copy(
                        placeId = "",
                        placeName = "",
                        roadAddress = "",
                        latitude = "",
                        longitude = "",
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = "장소가 삭제되었습니다. placeId=$placeId"
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "장소 삭제에 실패했습니다.",
                        successMessage = null
                    )
                }
            }
        }
    }

    fun resetForm() {
        _uiState.update {
            it.copy(
                placeId = "",
                reorderPlaceIdsInput = "",
                placeName = "",
                roadAddress = "",
                latitude = "",
                longitude = "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun validateForMutation(
        state: PlaceUiState,
        latitude: Double?,
        longitude: Double?
    ): String? {
        if (!isValidDateKey(state.dateKey)) {
            return "날짜는 yyyy-MM-dd 형식이어야 합니다."
        }
        if (latitude == null || longitude == null) {
            return "위도와 경도는 숫자로 입력해야 합니다."
        }
        if (!state.hasCompleteForm) {
            return "모든 필드를 입력해 주세요."
        }
        return null
    }

    private fun isValidReorderInput(rawInput: String, parsedIds: List<Long>): Boolean {
        val normalized = rawInput.split(',').map(String::trim).filter(String::isNotBlank)
        return normalized.isNotEmpty() && normalized.size == parsedIds.size
    }

    private fun mutationFailureMessage(isCreate: Boolean): String {
        return if (isCreate) {
            "장소 등록에 실패했습니다."
        } else {
            "장소 수정에 실패했습니다."
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
                updatePlaceUseCase = appContainer.updatePlaceUseCase,
                deletePlaceUseCase = appContainer.deletePlaceUseCase,
                reorderPlacesUseCase = appContainer.reorderPlacesUseCase,
                initialDateKey = initialDateKey
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
