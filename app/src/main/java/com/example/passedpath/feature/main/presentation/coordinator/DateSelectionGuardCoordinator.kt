package com.example.passedpath.feature.main.presentation.coordinator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DateSelectionGuardCoordinator {

    private val _state = MutableStateFlow(DateSelectionGuardState())
    val state: StateFlow<DateSelectionGuardState> = _state.asStateFlow()

    fun requestDateSelection(
        currentDateKey: String,
        targetDateKey: String,
        hasUnsavedDayNoteChanges: Boolean
    ): DateSelectionDecision {
        if (currentDateKey == targetDateKey) return DateSelectionDecision.Ignore

        return if (hasUnsavedDayNoteChanges) {
            _state.value = DateSelectionGuardState(pendingDateSelection = targetDateKey)
            DateSelectionDecision.RequireConfirmation
        } else {
            DateSelectionDecision.Proceed(targetDateKey)
        }
    }

    fun dismissPendingDateSelection() {
        _state.value = DateSelectionGuardState()
    }

    fun consumeDateSelectionAfterSave(
        isSubmitting: Boolean,
        hasUnsavedDayNoteChanges: Boolean,
        hasSaveError: Boolean,
        hasSaveSuccessMessage: Boolean
    ): String? {
        val targetDate = _state.value.pendingDateSelection ?: return null
        if (isSubmitting) return null

        return when {
            hasSaveError -> {
                dismissPendingDateSelection()
                null
            }

            !hasUnsavedDayNoteChanges && hasSaveSuccessMessage -> {
                dismissPendingDateSelection()
                targetDate
            }

            else -> null
        }
    }
}

data class DateSelectionGuardState(
    val pendingDateSelection: String? = null
)

sealed interface DateSelectionDecision {
    data object Ignore : DateSelectionDecision
    data object RequireConfirmation : DateSelectionDecision
    data class Proceed(val dateKey: String) : DateSelectionDecision
}
