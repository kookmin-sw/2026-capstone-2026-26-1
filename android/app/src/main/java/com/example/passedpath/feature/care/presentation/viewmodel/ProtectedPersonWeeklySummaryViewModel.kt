package com.example.passedpath.feature.care.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonWeeklyStatisticsUseCase
import com.example.passedpath.feature.summary.presentation.mapper.toWeeklySummaryContentUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtectedPersonWeeklySummaryViewModel(
    private val dependentUserId: Long,
    private val getProtectedPersonWeeklyStatisticsUseCase:
        GetProtectedPersonWeeklyStatisticsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeeklySummaryUiState())
    val uiState: StateFlow<WeeklySummaryUiState> = _uiState.asStateFlow()

    fun loadWeeklySummary(forceRefresh: Boolean = false) {
        val currentState = _uiState.value
        if (currentState.isLoading) return
        if (!forceRefresh && currentState.hasLoaded) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                getProtectedPersonWeeklyStatisticsUseCase(dependentUserId = dependentUserId)
            }.onSuccess { weeklyStatistics ->
                _uiState.update {
                    it.copy(
                        summary = weeklyStatistics.toWeeklySummaryContentUiState(),
                        hasLoaded = true,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiFailureMessage.fromThrowable(throwable)
                    )
                }
            }
        }
    }
}

class ProtectedPersonWeeklySummaryViewModelFactory(
    private val appContainer: AppContainer,
    private val dependentUserId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtectedPersonWeeklySummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProtectedPersonWeeklySummaryViewModel(
                dependentUserId = dependentUserId,
                getProtectedPersonWeeklyStatisticsUseCase =
                    appContainer.getProtectedPersonWeeklyStatisticsUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
