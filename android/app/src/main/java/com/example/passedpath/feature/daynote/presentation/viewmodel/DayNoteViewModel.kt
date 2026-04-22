package com.example.passedpath.feature.daynote.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteMemoUseCase
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteTitleUseCase
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayNoteSnapshotPatch(
    val dateKey: String,
    val title: String? = null,
    val memo: String? = null,
    val shouldUpdateTitle: Boolean = false,
    val shouldUpdateMemo: Boolean = false
)

class DayNoteViewModel(
    private val patchDayRouteTitleUseCase: PatchDayRouteTitleUseCase,
    private val patchDayRouteMemoUseCase: PatchDayRouteMemoUseCase,
    initialDateKey: String = todayDateKey()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayNoteUiState(dateKey = initialDateKey))
    val uiState: StateFlow<DayNoteUiState> = _uiState.asStateFlow()

    private val snapshotPatchChannel = Channel<DayNoteSnapshotPatch>(capacity = Channel.BUFFERED)
    val snapshotPatch = snapshotPatchChannel.receiveAsFlow()

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
                title = value.take(MAX_TITLE_LENGTH),
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun updateMemo(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                memo = value.take(MAX_MEMO_LENGTH),
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun submitDayNote() {
        val currentState = _uiState.value
        if (!isValidDateKey(currentState.dateKey)) {
            _uiState.update {
                it.copy(
                    errorMessage = "날짜는 yyyy-MM-dd 형식이어야 합니다.",
                    successMessage = null,
                    feedbackEventId = it.feedbackEventId + 1
                )
            }
            return
        }
        if (!currentState.isDirty) return

        val normalizedTitle = currentState.normalizedTitle
        val normalizedMemo = currentState.normalizedMemo
        val titleChanged = normalizedTitle != currentState.normalizedOriginalTitle
        val memoChanged = normalizedMemo != currentState.normalizedOriginalMemo

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }

            var savedTitle = currentState.originalTitle
            var savedMemo = currentState.originalMemo
            var titleSaved = false
            var memoSaved = false

            try {
                if (titleChanged) {
                    savedTitle = patchDayRouteTitleUseCase(
                        dateKey = currentState.dateKey,
                        title = normalizedTitle.ifBlank { null }
                    ).title.orEmpty()
                    titleSaved = true
                }

                if (memoChanged) {
                    savedMemo = patchDayRouteMemoUseCase(
                        dateKey = currentState.dateKey,
                        memo = normalizedMemo.ifBlank { null }
                    ).memo.orEmpty()
                    memoSaved = true
                }

                _uiState.update {
                    it.copy(
                        originalTitle = savedTitle,
                        originalMemo = savedMemo,
                        title = savedTitle,
                        memo = savedMemo,
                        isSubmitting = false,
                        errorMessage = null,
                        successMessage = when {
                            titleChanged && memoChanged -> "제목과 메모를 저장했습니다."
                            titleChanged -> "제목을 저장했습니다."
                            memoChanged -> "메모를 저장했습니다."
                            else -> null
                        },
                        feedbackEventId = it.feedbackEventId + 1
                    )
                }
                emitSnapshotPatch(
                    currentState = currentState,
                    savedTitle = savedTitle,
                    savedMemo = savedMemo,
                    titleSaved = titleSaved,
                    memoSaved = memoSaved
                )
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        originalTitle = if (titleSaved) savedTitle else it.originalTitle,
                        originalMemo = if (memoSaved) savedMemo else it.originalMemo,
                        title = if (titleSaved) savedTitle else it.title,
                        memo = if (memoSaved) savedMemo else it.memo,
                        isSubmitting = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable),
                        successMessage = null,
                        feedbackEventId = it.feedbackEventId + 1
                    )
                }
                emitSnapshotPatch(
                    currentState = currentState,
                    savedTitle = savedTitle,
                    savedMemo = savedMemo,
                    titleSaved = titleSaved,
                    memoSaved = memoSaved
                )
            }
        }
    }

    private suspend fun emitSnapshotPatch(
        currentState: DayNoteUiState,
        savedTitle: String,
        savedMemo: String,
        titleSaved: Boolean,
        memoSaved: Boolean
    ) {
        if (!titleSaved && !memoSaved) return

        snapshotPatchChannel.send(
            DayNoteSnapshotPatch(
                dateKey = currentState.dateKey,
                title = savedTitle,
                memo = savedMemo,
                shouldUpdateTitle = titleSaved,
                shouldUpdateMemo = memoSaved
            )
        )
    }

    private fun isValidDateKey(value: String): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
            isLenient = false
        }

        return try {
            formatter.parse(value)
            true
        } catch (_: ParseException) {
            false
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH: Int = 60
        const val MAX_MEMO_LENGTH: Int = 1000
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
