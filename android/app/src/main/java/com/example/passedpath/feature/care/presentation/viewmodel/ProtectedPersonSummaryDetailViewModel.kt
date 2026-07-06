package com.example.passedpath.feature.care.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonStatisticMetricUseCase
import com.example.passedpath.feature.summary.presentation.mapper.toEnterHomeTimeSummaryDetailUiState
import com.example.passedpath.feature.summary.presentation.mapper.toOutingTimeSummaryDetailUiState
import com.example.passedpath.feature.summary.presentation.mapper.toStatisticsPeriod
import com.example.passedpath.feature.summary.presentation.mapper.toSummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.mapper.toTotalOutingCountSummaryDetailUiState
import com.example.passedpath.feature.summary.presentation.mapper.toTotalOutingDurationSummaryDetailUiState
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailPeriod
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailViewModelUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtectedPersonSummaryDetailViewModel(
    private val dependentUserId: Long,
    private val getProtectedPersonStatisticMetricUseCase:
        GetProtectedPersonStatisticMetricUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SummaryDetailViewModelUiState())
    val uiState: StateFlow<SummaryDetailViewModelUiState> = _uiState.asStateFlow()
    private var fetchJob: Job? = null

    fun loadSummaryDetail(
        metric: SummaryDetailMetric,
        forceRefresh: Boolean = false
    ) {
        if (!metric.isProtectedPersonApiBackedMetric()) return

        val currentState = _uiState.value
        if (currentState.isLoading && currentState.metric == metric) return
        if (!forceRefresh && currentState.hasLoaded && currentState.metric == metric) return

        fetchSummaryDetail(metric = metric, period = currentState.selectedPeriod)
    }

    fun selectPeriod(
        metric: SummaryDetailMetric,
        period: SummaryDetailPeriod
    ) {
        if (!metric.isProtectedPersonApiBackedMetric()) return

        val currentState = _uiState.value
        if (
            currentState.metric == metric &&
            currentState.selectedPeriod == period &&
            currentState.hasLoaded
        ) {
            return
        }
        if (
            currentState.metric == metric &&
            currentState.isLoading &&
            currentState.selectedPeriod == period
        ) {
            return
        }

        _uiState.update { state ->
            state.copy(
                metric = metric,
                selectedPeriod = period
            )
        }
        fetchSummaryDetail(metric = metric, period = period)
    }

    private fun fetchSummaryDetail(
        metric: SummaryDetailMetric,
        period: SummaryDetailPeriod
    ) {
        if (!metric.isProtectedPersonApiBackedMetric()) return

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update { state ->
                val isChangingMetric = state.metric != metric
                state.copy(
                    metric = metric,
                    selectedPeriod = period,
                    content = if (isChangingMetric) null else state.content,
                    hasLoaded = if (isChangingMetric) false else state.hasLoaded,
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                when (metric) {
                    SummaryDetailMetric.OUTING_TIME -> {
                        getProtectedPersonStatisticMetricUseCase.getOutingTime(
                            dependentUserId = dependentUserId,
                            period = period.toStatisticsPeriod()
                        )
                    }
                    SummaryDetailMetric.ENTER_HOME_TIME -> {
                        getProtectedPersonStatisticMetricUseCase.getEnterHomeTime(
                            dependentUserId = dependentUserId,
                            period = period.toStatisticsPeriod()
                        )
                    }
                    SummaryDetailMetric.TOTAL_OUTING_DURATION -> {
                        getProtectedPersonStatisticMetricUseCase.getTotalOutingSeconds(
                            dependentUserId = dependentUserId,
                            period = period.toStatisticsPeriod()
                        )
                    }
                    SummaryDetailMetric.TOTAL_OUTING_COUNT -> {
                        getProtectedPersonStatisticMetricUseCase.getTotalOutingCount(
                            dependentUserId = dependentUserId,
                            period = period.toStatisticsPeriod()
                        )
                    }
                    SummaryDetailMetric.VISITS -> {
                        error("Unsupported protected person summary detail metric: $metric")
                    }
                }
            }.onSuccess { statisticMetric ->
                _uiState.update {
                    it.copy(
                        metric = metric,
                        selectedPeriod = statisticMetric.period.toSummaryDetailPeriod(),
                        content = when (metric) {
                            SummaryDetailMetric.OUTING_TIME -> {
                                statisticMetric.toOutingTimeSummaryDetailUiState()
                            }
                            SummaryDetailMetric.ENTER_HOME_TIME -> {
                                statisticMetric.toEnterHomeTimeSummaryDetailUiState()
                            }
                            SummaryDetailMetric.TOTAL_OUTING_DURATION -> {
                                statisticMetric.toTotalOutingDurationSummaryDetailUiState()
                            }
                            SummaryDetailMetric.TOTAL_OUTING_COUNT -> {
                                statisticMetric.toTotalOutingCountSummaryDetailUiState()
                            }
                            SummaryDetailMetric.VISITS -> null
                        },
                        hasLoaded = true,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable

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

private fun SummaryDetailMetric.isProtectedPersonApiBackedMetric(): Boolean {
    return this == SummaryDetailMetric.OUTING_TIME ||
        this == SummaryDetailMetric.ENTER_HOME_TIME ||
        this == SummaryDetailMetric.TOTAL_OUTING_DURATION ||
        this == SummaryDetailMetric.TOTAL_OUTING_COUNT
}

class ProtectedPersonSummaryDetailViewModelFactory(
    private val appContainer: AppContainer,
    private val dependentUserId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtectedPersonSummaryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProtectedPersonSummaryDetailViewModel(
                dependentUserId = dependentUserId,
                getProtectedPersonStatisticMetricUseCase =
                    appContainer.getProtectedPersonStatisticMetricUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
