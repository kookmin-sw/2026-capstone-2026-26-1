package com.example.passedpath.feature.care.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonVisitStatisticsUseCase
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

class ProtectedPersonVisitStatisticsViewModel(
    private val dependentUserId: Long,
    private val getProtectedPersonVisitStatisticsUseCase:
        GetProtectedPersonVisitStatisticsUseCase
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
                getProtectedPersonVisitStatisticsUseCase(
                    dependentUserId = dependentUserId,
                    period = period.toStatisticsPeriod()
                )
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

class ProtectedPersonVisitStatisticsViewModelFactory(
    private val appContainer: AppContainer,
    private val dependentUserId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtectedPersonVisitStatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProtectedPersonVisitStatisticsViewModel(
                dependentUserId = dependentUserId,
                getProtectedPersonVisitStatisticsUseCase =
                    appContainer.getProtectedPersonVisitStatisticsUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
