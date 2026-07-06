package com.example.passedpath.feature.summary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.summary.domain.usecase.GetWeeklyStatisticsUseCase
import com.example.passedpath.feature.summary.presentation.mapper.toWeeklySummaryContentUiState
import com.example.passedpath.feature.summary.presentation.state.WeeklySummaryUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeeklySummaryViewModel(
    private val getWeeklyStatisticsUseCase: GetWeeklyStatisticsUseCase
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
                getWeeklyStatisticsUseCase()
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

class WeeklySummaryViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeeklySummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeeklySummaryViewModel(
                getWeeklyStatisticsUseCase = appContainer.getWeeklyStatisticsUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
