package com.example.passedpath.feature.care.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.care.domain.usecase.AcceptCareRelationshipInviteUseCase
import com.example.passedpath.feature.care.presentation.state.CareInviteAcceptUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CareInviteAcceptViewModel(
    private val acceptCareRelationshipInviteUseCase: AcceptCareRelationshipInviteUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CareInviteAcceptUiState())
    val uiState: StateFlow<CareInviteAcceptUiState> = _uiState.asStateFlow()

    fun openInvite(inviteCode: String) {
        val resolvedInviteCode = inviteCode.trim().takeIf(String::isNotEmpty) ?: return

        _uiState.update { state ->
            state.copy(
                isVisible = true,
                inviteCode = resolvedInviteCode,
                isSubmitting = false,
                errorMessage = null
            )
        }
    }

    fun dismissInvite() {
        _uiState.update { state ->
            if (state.isSubmitting) {
                state
            } else {
                CareInviteAcceptUiState(successEventId = state.successEventId)
            }
        }
    }

    fun acceptInvite() {
        val inviteCode = _uiState.value.inviteCode?.trim()
            ?.takeIf(String::isNotEmpty) ?: return

        if (_uiState.value.isSubmitting) return

        _uiState.update { state ->
            state.copy(
                isSubmitting = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            runCatching {
                acceptCareRelationshipInviteUseCase(inviteCode)
            }.onSuccess {
                _uiState.update { state ->
                    CareInviteAcceptUiState(successEventId = state.successEventId + 1L)
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        errorMessage = CareInviteAcceptErrorMessage
                    )
                }
            }
        }
    }

    private companion object {
        const val CareInviteAcceptErrorMessage = "Failed to accept care invite"
    }
}

class CareInviteAcceptViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CareInviteAcceptViewModel::class.java)) {
            return CareInviteAcceptViewModel(
                acceptCareRelationshipInviteUseCase =
                    appContainer.acceptCareRelationshipInviteUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
