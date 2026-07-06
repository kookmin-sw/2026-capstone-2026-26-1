package com.example.passedpath.feature.care.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteListItem
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteListResult
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDayRouteListUseCase
import com.example.passedpath.feature.care.presentation.mapper.toProtectedPersonRouteDateUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteHistoryUiState
import com.example.passedpath.ui.state.ApiFailureMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtectedPersonRouteHistoryViewModel(
    private val dependentUserId: Long,
    dependentNickname: String,
    private val getProtectedPersonDayRouteListUseCase: GetProtectedPersonDayRouteListUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProtectedPersonRouteHistoryUiState(dependentNickname = dependentNickname)
    )
    val uiState: StateFlow<ProtectedPersonRouteHistoryUiState> = _uiState.asStateFlow()

    fun fetchRoutes(forceRefresh: Boolean = false) {
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

            when (
                val result = getProtectedPersonDayRouteListUseCase(
                    dependentUserId = dependentUserId
                )
            ) {
                is ProtectedPersonDayRouteListResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            dayRoutes = result.dayRouteList.dayRoutes.toUiStates(),
                            hasNext = result.dayRouteList.hasNext,
                            nextCursorDate = result.dayRouteList.nextCursorDate,
                            hasLoaded = true,
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = null
                        )
                    }
                }

                is ProtectedPersonDayRouteListResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            hasLoaded = state.hasLoaded,
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = ApiFailureMessage.fromThrowable(result.throwable)
                        )
                    }
                }
            }
        }
    }

    fun fetchNextRoutes() {
        val currentState = _uiState.value
        val cursorDate = currentState.nextCursorDate ?: return
        if (
            !currentState.hasLoaded ||
            !currentState.hasNext ||
            currentState.isLoading ||
            currentState.isLoadingMore
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoadingMore = true,
                    errorMessage = null
                )
            }

            when (
                val result = getProtectedPersonDayRouteListUseCase(
                    dependentUserId = dependentUserId,
                    cursorDate = cursorDate
                )
            ) {
                is ProtectedPersonDayRouteListResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            dayRoutes = (state.dayRoutes + result.dayRouteList.dayRoutes.toUiStates())
                                .distinctBy { route -> route.dateKey },
                            hasNext = result.dayRouteList.hasNext,
                            nextCursorDate = result.dayRouteList.nextCursorDate,
                            hasLoaded = true,
                            isLoadingMore = false,
                            errorMessage = null
                        )
                    }
                }

                is ProtectedPersonDayRouteListResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoadingMore = false,
                            errorMessage = ApiFailureMessage.fromThrowable(result.throwable)
                        )
                    }
                }
            }
        }
    }
}

class ProtectedPersonRouteHistoryViewModelFactory(
    private val appContainer: AppContainer,
    private val dependentUserId: Long,
    private val dependentNickname: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtectedPersonRouteHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProtectedPersonRouteHistoryViewModel(
                dependentUserId = dependentUserId,
                dependentNickname = dependentNickname,
                getProtectedPersonDayRouteListUseCase =
                    appContainer.getProtectedPersonDayRouteListUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun List<ProtectedPersonDayRouteListItem>.toUiStates() =
    map(ProtectedPersonDayRouteListItem::toProtectedPersonRouteDateUiState)
