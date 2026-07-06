package com.example.passedpath.feature.summary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.summary.domain.usecase.GetVisitStatisticsUseCase
import com.example.passedpath.feature.summary.presentation.mapper.toStatisticsPeriod
import com.example.passedpath.feature.summary.presentation.mapper.toSummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.mapper.toVisitStatisticsContentUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.state.VisitStatisticsUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VisitStatisticsViewModel(
    private val getVisitStatisticsUseCase: GetVisitStatisticsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(VisitStatisticsUiState())
    val uiState: StateFlow<VisitStatisticsUiState> = _uiState.asStateFlow()

    fun loadVisitStatistics(forceRefresh: Boolean = false) {
        val currentState = _uiState.value
        if (currentState.isLoading) return
        if (!forceRefresh && currentState.hasLoaded) return

        fetchVisitStatistics(period = currentState.selectedPeriod)
    }

    fun selectPeriod(period: SummaryDetailPeriod) {
        val currentState = _uiState.value
        if (currentState.selectedPeriod == period && currentState.hasLoaded) return
        if (currentState.isLoading && currentState.selectedPeriod == period) return

        _uiState.update { state ->
            state.copy(selectedPeriod = period)
        }
        fetchVisitStatistics(period = period)
    }

    private fun fetchVisitStatistics(period: SummaryDetailPeriod) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    selectedPeriod = period,
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                getVisitStatisticsUseCase(period = period.toStatisticsPeriod())
            }.onSuccess { visitStatistics ->
                _uiState.update {
                    it.copy(
                        selectedPeriod = visitStatistics.period.toSummaryDetailPeriod(),
                        content = visitStatistics.toVisitStatisticsContentUiState(),
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

class VisitStatisticsViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitStatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitStatisticsViewModel(
                getVisitStatisticsUseCase = appContainer.getVisitStatisticsUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
