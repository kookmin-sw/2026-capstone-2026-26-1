package com.example.passedpath.feature.care.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceResult
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDayRouteUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDaySummaryUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonVisitedPlacesUseCase
import com.example.passedpath.feature.care.presentation.mapper.toProtectedPersonPlaceListUiState
import com.example.passedpath.feature.care.presentation.mapper.toProtectedPersonRouteDetailDateText
import com.example.passedpath.feature.care.presentation.mapper.toProtectedPersonRouteMapUiState
import com.example.passedpath.feature.care.presentation.mapper.toProtectedPersonSummaryContentUiState
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonPlaceListUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteDetailUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteMapUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonSummaryUiState
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtectedPersonRouteDetailViewModel(
    private val dependentUserId: Long,
    dependentNickname: String,
    private val dateKey: String,
    private val getProtectedPersonDayRouteUseCase: GetProtectedPersonDayRouteUseCase,
    private val getProtectedPersonVisitedPlacesUseCase: GetProtectedPersonVisitedPlacesUseCase,
    private val getProtectedPersonDaySummaryUseCase: GetProtectedPersonDaySummaryUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProtectedPersonRouteDetailUiState(
            dependentNickname = dependentNickname,
            dateKey = dateKey,
            route = ProtectedPersonRouteMapUiState(
                dateKey = dateKey,
                dateText = dateKey.toProtectedPersonRouteDetailDateText()
            )
        )
    )
    val uiState: StateFlow<ProtectedPersonRouteDetailUiState> = _uiState.asStateFlow()
    private var isPlaceGuideBannerDismissed: Boolean = false

    fun load() {
        loadRoute()
        loadPlaces()
        loadSummary()
    }

    fun retryRoute() {
        loadRoute()
    }

    fun retryPlaces() {
        loadPlaces()
    }

    fun retrySummary() {
        loadSummary()
    }

    fun selectBottomSheetTab(tab: ProtectedPersonBottomSheetTab) {
        _uiState.update { state ->
            state.copy(
                selectedBottomSheetTab = tab,
                requestedSheetValue = if (state.bottomSheetValue == BaseBottomSheetValue.HIDDEN) {
                    BaseBottomSheetValue.MIDDLE
                } else {
                    state.requestedSheetValue
                }
            )
        }
    }

    fun onSheetValueChanged(bottomSheetValue: BaseBottomSheetValue) {
        _uiState.update { state ->
            state.copy(
                bottomSheetValue = bottomSheetValue,
                selectedPlaceId = if (bottomSheetValue == BaseBottomSheetValue.HIDDEN) {
                    null
                } else {
                    state.selectedPlaceId
                }
            )
        }
    }

    fun onSheetCommandConsumed(consumedValue: BaseBottomSheetValue) {
        _uiState.update { state ->
            if (state.requestedSheetValue == consumedValue) {
                state.copy(requestedSheetValue = null)
            } else {
                state
            }
        }
    }

    fun onPlaceMarkerClick(placeId: Long) {
        _uiState.update { state ->
            state.copy(
                selectedBottomSheetTab = ProtectedPersonBottomSheetTab.PLACE,
                selectedPlaceId = placeId,
                requestedSheetValue = BaseBottomSheetValue.EXPANDED
            )
        }
    }

    fun onPlaceCardClick(placeId: Long) {
        _uiState.update { state ->
            state.copy(
                requestedSheetValue = BaseBottomSheetValue.HIDDEN,
                selectedPlaceId = null,
                focusedPlaceId = placeId
            )
        }
    }

    fun onSelectedPlaceHandled() {
        _uiState.update { state -> state.copy(selectedPlaceId = null) }
    }

    fun onFocusedPlaceHandled() {
        _uiState.update { state -> state.copy(focusedPlaceId = null) }
    }

    fun onMapClick() {
        _uiState.update { state ->
            state.copy(
                requestedSheetValue = BaseBottomSheetValue.HIDDEN,
                selectedPlaceId = null,
                focusedPlaceId = null
            )
        }
    }

    fun dismissPlaceGuideBanner() {
        isPlaceGuideBannerDismissed = true
        _uiState.update { state ->
            state.copy(
                placeListUiState = state.placeListUiState.copy(
                    isPlaceGuideBannerVisible = false
                )
            )
        }
    }

    private fun loadRoute() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isRouteLoading = true,
                    isRouteEmpty = false,
                    routeErrorMessage = null
                )
            }

            when (
                val result = getProtectedPersonDayRouteUseCase(
                    dependentUserId = dependentUserId,
                    dateKey = dateKey
                )
            ) {
                ProtectedPersonDayRouteResult.Empty -> {
                    _uiState.update { state ->
                        state.copy(
                            isRouteLoading = false,
                            isRouteEmpty = true,
                            routeErrorMessage = null
                        )
                    }
                }

                is ProtectedPersonDayRouteResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isRouteLoading = false,
                            isRouteEmpty = false,
                            routeErrorMessage = ProtectedPersonRouteLoadErrorMessage
                        )
                    }
                }

                is ProtectedPersonDayRouteResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            route = result.routeDetail.toProtectedPersonRouteMapUiState(),
                            isRouteLoading = false,
                            isRouteEmpty = !result.routeDetail.routePoints.hasUsableRouteData(),
                            routeErrorMessage = null
                        )
                    }
                }
            }
        }
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    placeListUiState = state.placeListUiState.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                ).withPlaceGuideBannerVisibility()
            }

            when (
                val result = getProtectedPersonVisitedPlacesUseCase(
                    dependentUserId = dependentUserId,
                    dateKey = dateKey
                )
            ) {
                ProtectedPersonVisitedPlaceResult.Empty -> {
                    _uiState.update { state ->
                        state.copy(
                            placeListUiState = ProtectedPersonPlaceListUiState(
                                hasLoaded = true
                            )
                        ).withPlaceGuideBannerVisibility()
                    }
                }

                is ProtectedPersonVisitedPlaceResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            placeListUiState = state.placeListUiState.copy(
                                hasLoaded = true,
                                isLoading = false,
                                errorMessage = ProtectedPersonPlacesLoadErrorMessage
                            )
                        ).withPlaceGuideBannerVisibility()
                    }
                }

                is ProtectedPersonVisitedPlaceResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            placeListUiState = result.placeList
                                .toProtectedPersonPlaceListUiState()
                        ).withPlaceGuideBannerVisibility()
                    }
                }
            }
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    summaryUiState = state.summaryUiState.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                )
            }

            when (
                val result = getProtectedPersonDaySummaryUseCase(
                    dependentUserId = dependentUserId,
                    dateKey = dateKey
                )
            ) {
                ProtectedPersonDaySummaryResult.Empty -> {
                    _uiState.update { state ->
                        state.copy(
                            summaryUiState = ProtectedPersonSummaryUiState(
                                hasLoaded = true
                            )
                        )
                    }
                }

                is ProtectedPersonDaySummaryResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            summaryUiState = state.summaryUiState.copy(
                                hasLoaded = true,
                                isLoading = false,
                                errorMessage = ProtectedPersonSummaryLoadErrorMessage
                            )
                        )
                    }
                }

                is ProtectedPersonDaySummaryResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            summaryUiState = ProtectedPersonSummaryUiState(
                                hasLoaded = true,
                                summary = result.daySummary
                                    .toProtectedPersonSummaryContentUiState()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun ProtectedPersonRouteDetailUiState.withPlaceGuideBannerVisibility():
        ProtectedPersonRouteDetailUiState {
        return copy(
            placeListUiState = placeListUiState.copy(
                isPlaceGuideBannerVisible = !isPlaceGuideBannerDismissed
            )
        )
    }

    private companion object {
        const val ProtectedPersonRouteLoadErrorMessage =
            "Failed to load protected person route"
        const val ProtectedPersonPlacesLoadErrorMessage =
            "Failed to load protected person places"
        const val ProtectedPersonSummaryLoadErrorMessage =
            "Failed to load protected person summary"
    }
}

class ProtectedPersonRouteDetailViewModelFactory(
    private val appContainer: AppContainer,
    private val dependentUserId: Long,
    private val dependentNickname: String,
    private val dateKey: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtectedPersonRouteDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProtectedPersonRouteDetailViewModel(
                dependentUserId = dependentUserId,
                dependentNickname = dependentNickname,
                dateKey = dateKey,
                getProtectedPersonDayRouteUseCase =
                    appContainer.getProtectedPersonDayRouteUseCase,
                getProtectedPersonVisitedPlacesUseCase =
                    appContainer.getProtectedPersonVisitedPlacesUseCase,
                getProtectedPersonDaySummaryUseCase =
                    appContainer.getProtectedPersonDaySummaryUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun List<*>.hasUsableRouteData(): Boolean {
    return size > 0
}
