package com.example.passedpath.feature.daynote.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteMemoUseCase
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteTitleUseCase
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
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

class DayNoteViewModel(
    private val patchDayRouteTitleUseCase: PatchDayRouteTitleUseCase,
    private val patchDayRouteMemoUseCase: PatchDayRouteMemoUseCase,
    initialDateKey: String = todayDateKey()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayNoteUiState(dateKey = initialDateKey))
    val uiState: StateFlow<DayNoteUiState> = _uiState.asStateFlow()

    fun syncSelectedDay(dateKey: String, title: String, memo: String) {
        _uiState.update { currentState ->
            if (
                currentState.dateKey == dateKey &&
                currentState.originalTitle == title &&
                currentState.originalMemo == memo
            ) {
                currentState
            } else {
                currentState.copy(
                    dateKey = dateKey,
                    originalTitle = title,
                    originalMemo = memo,
                    title = title,
                    memo = memo,
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun updateMemo(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                memo = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun submitTitle() {
        val currentState = _uiState.value
        if (!isValidDateKey(currentState.dateKey)) {
            _uiState.update {
                it.copy(
                    errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다.",
                    successMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                val result = patchDayRouteTitleUseCase(
                    dateKey = currentState.dateKey,
                    title = currentState.title
                )
                val savedTitle = result.title.orEmpty()
                val successMessage = if (result.title.isNullOrBlank()) {
                    "제목이 삭제되었습니다."
                } else {
                    "제목이 저장되었습니다."
                }
                _uiState.update {
                    it.copy(
                        originalTitle = savedTitle,
                        title = savedTitle,
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = successMessage
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "제목 저장에 실패했습니다.",
                        successMessage = null
                    )
                }
            }
        }
    }

    fun submitMemo() {
        val currentState = _uiState.value
        if (!isValidDateKey(currentState.dateKey)) {
            _uiState.update {
                it.copy(
                    errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다.",
                    successMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                val result = patchDayRouteMemoUseCase(
                    dateKey = currentState.dateKey,
                    memo = currentState.memo
                )
                val savedMemo = result.memo.orEmpty()
                val successMessage = if (result.memo.isNullOrBlank()) {
                    "메모가 삭제되었습니다."
                } else {
                    "메모가 저장되었습니다."
                }
                _uiState.update {
                    it.copy(
                        originalMemo = savedMemo,
                        memo = savedMemo,
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = successMessage
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "메모 저장에 실패했습니다.",
                        successMessage = null
                    )
                }
            }
        }
    }

    fun clearTitle() {
        _uiState.update { currentState ->
            currentState.copy(
                title = "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun clearMemo() {
        _uiState.update { currentState ->
            currentState.copy(
                memo = "",
                errorMessage = null,
                successMessage = null
            )
        }
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

class DayNoteViewModelFactory(
    private val appContainer: AppContainer,
    private val initialDateKey: String = todayDateKey()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DayNoteViewModel(
                patchDayRouteTitleUseCase = appContainer.patchDayRouteTitleUseCase,
                patchDayRouteMemoUseCase = appContainer.patchDayRouteMemoUseCase,
                initialDateKey = initialDateKey
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
