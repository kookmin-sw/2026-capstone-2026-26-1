package com.example.passedpath.feature.care.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.care.domain.model.CareDependentLocationStreamEvent
import com.example.passedpath.feature.care.domain.model.CareLatestGpsPoint
import com.example.passedpath.feature.care.domain.repository.CareGuideRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonVisitedPlaceResult
import com.example.passedpath.feature.care.domain.usecase.CreateCareRelationshipInviteLinkUseCase
import com.example.passedpath.feature.care.domain.usecase.GetCareDependentsUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDayRouteUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonDaySummaryUseCase
import com.example.passedpath.feature.care.domain.usecase.GetProtectedPersonVisitedPlacesUseCase
import com.example.passedpath.feature.care.domain.usecase.ObserveCareDependentLocationStreamUseCase
import com.example.passedpath.feature.care.presentation.mapper.toCareDependentMapMarkerUiStates
import com.example.passedpath.feature.care.presentation.mapper.toCareDependentUserUiState
import com.example.passedpath.feature.care.presentation.mapper.toProtectedPersonPlaceListUiState
import com.example.passedpath.feature.care.presentation.mapper.toProtectedPersonRouteMapUiState
import com.example.passedpath.feature.care.presentation.mapper.toProtectedPersonSummaryContentUiState
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.feature.care.presentation.state.CareUiState
import com.example.passedpath.feature.care.presentation.state.CareInviteUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonPlaceListUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonRouteMapUiState
import com.example.passedpath.feature.care.presentation.state.ProtectedPersonSummaryUiState
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetValue
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CareViewModel(
    private val getCareDependentsUseCase: GetCareDependentsUseCase,
    private val getProtectedPersonDayRouteUseCase: GetProtectedPersonDayRouteUseCase,
    private val getProtectedPersonVisitedPlacesUseCase: GetProtectedPersonVisitedPlacesUseCase,
    private val getProtectedPersonDaySummaryUseCase: GetProtectedPersonDaySummaryUseCase,
    private val createCareRelationshipInviteLinkUseCase: CreateCareRelationshipInviteLinkUseCase,
    private val observeCareDependentLocationStreamUseCase:
        ObserveCareDependentLocationStreamUseCase,
    private val careGuideRepository: CareGuideRepository = InMemoryCareGuideRepository(),
    private val todayDateKeyProvider: () -> String = ::defaultKstTodayDateKey
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        CareUiState(selectedDateKey = todayDateKeyProvider())
    )
    val uiState: StateFlow<CareUiState> = _uiState.asStateFlow()
    private var inviteRequestId: Long = 0L
    private var locationStreamJob: Job? = null
    private var isProtectedPersonPlaceGuideBannerDismissed = false
    private var hasRequestedProtectedPersonPlaceGuideBannerDismissal = false

    init {
        viewModelScope.launch {
            careGuideRepository.isProtectedPersonPlaceGuideBannerDismissed.collect { dismissed ->
                isProtectedPersonPlaceGuideBannerDismissed =
                    dismissed || hasRequestedProtectedPersonPlaceGuideBannerDismissal
                _uiState.update { state ->
                    state.withProtectedPersonPlaceGuideBannerVisibility(
                        isProtectedPersonPlaceGuideBannerDismissed
                    )
                }
            }
        }
        refreshDependents()
    }

    fun refreshDependents() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null)
            }

            runCatching {
                getCareDependentsUseCase()
            }.onSuccess { dependentUserList ->
                val dependents = dependentUserList.dependentUsers
                    .map { dependent -> dependent.toCareDependentUserUiState() }
                val mapMarkers = dependents.toCareDependentMapMarkerUiStates()

                _uiState.update { state ->
                    val retainedSelectedId = state.selectedDependentUserId
                        ?.takeIf { selectedId ->
                            dependents.any { dependent ->
                                dependent.dependentUserId == selectedId
                            }
                        }

                    state.copy(
                        dependents = dependents,
                        mapMarkers = mapMarkers,
                        selectedDependentUserId = retainedSelectedId,
                        route = if (retainedSelectedId == null) {
                            ProtectedPersonRouteMapUiState()
                        } else {
                            state.route
                        },
                        isRouteLoading = retainedSelectedId != null && state.isRouteLoading,
                        hasRouteLoaded = retainedSelectedId != null && state.hasRouteLoaded,
                        isRouteEmpty = retainedSelectedId != null && state.isRouteEmpty,
                        routeErrorMessage = if (retainedSelectedId == null) {
                            null
                        } else {
                            state.routeErrorMessage
                        },
                        placeListUiState = if (retainedSelectedId == null) {
                            ProtectedPersonPlaceListUiState()
                        } else {
                            state.placeListUiState
                        },
                        summaryUiState = if (retainedSelectedId == null) {
                            ProtectedPersonSummaryUiState()
                        } else {
                            state.summaryUiState
                        },
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = null
                    ).withProtectedPersonPlaceGuideBannerVisibility(
                        isProtectedPersonPlaceGuideBannerDismissed
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = CareDependentsLoadErrorMessage
                    )
                }
            }
        }
    }

    fun startLocationStream() {
        if (locationStreamJob?.isActive == true) return

        locationStreamJob = viewModelScope.launch {
            observeCareDependentLocationStreamUseCase().collect { event ->
                handleLocationStreamEvent(event)
            }
        }
    }

    fun stopLocationStream() {
        locationStreamJob?.cancel()
        locationStreamJob = null
    }

    fun retryLocationStream() {
        stopLocationStream()
        _uiState.update { state ->
            state.copy(locationStreamErrorMessage = null)
        }
        startLocationStream()
    }

    fun dismissLocationStreamError() {
        _uiState.update { state ->
            state.copy(locationStreamErrorMessage = null)
        }
    }

    fun selectDependent(dependentUserId: Long?) {
        if (dependentUserId == null) {
            _uiState.update { state ->
                state.copy(
                    selectedDependentUserId = null,
                    selectedBottomSheetTab = ProtectedPersonBottomSheetTab.PLACE,
                    requestedSheetValue = BaseBottomSheetValue.HIDDEN,
                    selectedPlaceId = null,
                    focusedPlaceId = null,
                    route = ProtectedPersonRouteMapUiState(),
                    isRouteLoading = false,
                    hasRouteLoaded = false,
                    isRouteEmpty = false,
                    routeErrorMessage = null,
                    placeListUiState = ProtectedPersonPlaceListUiState(),
                    summaryUiState = ProtectedPersonSummaryUiState()
                )
            }
            return
        }

        val currentState = _uiState.value
        val selectedId = dependentUserId.takeIf { requestedId ->
            currentState.dependents.any { dependent ->
                dependent.dependentUserId == requestedId
            }
        } ?: return
        val dateKey = currentState.selectedDateKey.ifBlank { todayDateKeyProvider() }
        val isSameDependent = currentState.selectedDependentUserId == selectedId

        _uiState.update { state ->
            state.copy(
                selectedDependentUserId = selectedId,
                selectedDateKey = dateKey,
                selectedBottomSheetTab = ProtectedPersonBottomSheetTab.PLACE,
                requestedSheetValue = BaseBottomSheetValue.MIDDLE,
                selectedPlaceId = null,
                focusedPlaceId = null,
                route = if (isSameDependent && state.hasRouteLoaded) {
                    state.route
                } else {
                    ProtectedPersonRouteMapUiState(dateKey = dateKey)
                },
                isRouteLoading = !isSameDependent || !state.hasRouteLoaded,
                hasRouteLoaded = isSameDependent && state.hasRouteLoaded,
                isRouteEmpty = isSameDependent && state.hasRouteLoaded && state.isRouteEmpty,
                routeErrorMessage = if (isSameDependent && state.hasRouteLoaded) {
                    state.routeErrorMessage
                } else {
                    null
                },
                placeListUiState = if (isSameDependent && state.placeListUiState.hasLoaded) {
                    state.placeListUiState
                } else {
                    ProtectedPersonPlaceListUiState(isLoading = true)
                },
                summaryUiState = if (isSameDependent && state.summaryUiState.hasLoaded) {
                    state.summaryUiState
                } else {
                    ProtectedPersonSummaryUiState(isLoading = true)
                }
            ).withProtectedPersonPlaceGuideBannerVisibility(
                isProtectedPersonPlaceGuideBannerDismissed
            )
        }

        if (!isSameDependent ||
            !currentState.hasRouteLoaded ||
            !currentState.placeListUiState.hasLoaded ||
            !currentState.summaryUiState.hasLoaded
        ) {
            loadProtectedPersonDetails(
                dependentUserId = selectedId,
                dateKey = dateKey
            )
        }
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
            if (bottomSheetValue == BaseBottomSheetValue.HIDDEN &&
                state.requestedSheetValue == null
            ) {
                state.copy(
                    bottomSheetValue = bottomSheetValue,
                    selectedPlaceId = null
                )
            } else {
                state.copy(bottomSheetValue = bottomSheetValue)
            }
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

    fun openInviteModal() {
        createInviteLink()
    }

    fun retryCreateInviteLink() {
        if (_uiState.value.inviteUiState.isLoading) return
        createInviteLink()
    }

    fun dismissInviteModal() {
        inviteRequestId++
        _uiState.update { state ->
            state.copy(inviteUiState = CareInviteUiState())
        }
    }

    fun onInviteLinkCopied() {
        _uiState.update { state ->
            state.copy(
                inviteUiState = state.inviteUiState.copy(
                    copyFeedbackEventId = state.inviteUiState.copyFeedbackEventId + 1
                )
            )
        }
    }

    fun retryProtectedPersonPlaces() {
        val state = _uiState.value
        val dependentUserId = state.selectedDependentUserId ?: return
        loadProtectedPersonPlaces(
            dependentUserId = dependentUserId,
            dateKey = state.selectedDateKey
        )
    }

    fun retryProtectedPersonRoute() {
        val state = _uiState.value
        val dependentUserId = state.selectedDependentUserId ?: return
        loadProtectedPersonRoute(
            dependentUserId = dependentUserId,
            dateKey = state.selectedDateKey
        )
    }

    fun dismissProtectedPersonPlaceGuideBanner() {
        if (isProtectedPersonPlaceGuideBannerDismissed) return

        hasRequestedProtectedPersonPlaceGuideBannerDismissal = true
        isProtectedPersonPlaceGuideBannerDismissed = true
        _uiState.update { state ->
            state.copy(
                placeListUiState = state.placeListUiState.copy(
                    isPlaceGuideBannerVisible = false
                )
            )
        }

        viewModelScope.launch {
            careGuideRepository.dismissProtectedPersonPlaceGuideBanner()
        }
    }

    fun retryProtectedPersonSummary() {
        val state = _uiState.value
        val dependentUserId = state.selectedDependentUserId ?: return
        loadProtectedPersonSummary(
            dependentUserId = dependentUserId,
            dateKey = state.selectedDateKey
        )
    }

    private fun createInviteLink() {
        val requestId = ++inviteRequestId
        _uiState.update { state ->
            state.copy(
                inviteUiState = CareInviteUiState(
                    isVisible = true,
                    isLoading = true
                )
            )
        }

        viewModelScope.launch {
            runCatching {
                createCareRelationshipInviteLinkUseCase()
            }.onSuccess { inviteLink ->
                _uiState.update { state ->
                    if (requestId != inviteRequestId || !state.inviteUiState.isVisible) {
                        state
                    } else {
                        state.copy(
                            inviteUiState = state.inviteUiState.copy(
                                isLoading = false,
                                inviteLink = inviteLink.inviteLink,
                                errorMessage = null
                            )
                        )
                    }
                }
            }.onFailure {
                _uiState.update { state ->
                    if (requestId != inviteRequestId || !state.inviteUiState.isVisible) {
                        state
                    } else {
                        state.copy(
                            inviteUiState = state.inviteUiState.copy(
                                isLoading = false,
                                inviteLink = null,
                                errorMessage = CareInviteLinkCreateErrorMessage
                            )
                        )
                    }
                }
            }
        }
    }

    private fun handleLocationStreamEvent(event: CareDependentLocationStreamEvent) {
        when (event) {
            is CareDependentLocationStreamEvent.Connected -> {
                _uiState.update { state ->
                    state.copy(locationStreamErrorMessage = null)
                }
            }

            is CareDependentLocationStreamEvent.Error -> {
                _uiState.update { state ->
                    state.copy(locationStreamErrorMessage = CareLocationStreamErrorMessage)
                }
            }

            is CareDependentLocationStreamEvent.LocationUpdated -> {
                _uiState.update { state ->
                    state.withUpdatedDependentLocation(
                        dependentUserId = event.dependentUserId,
                        latestGpsPoint = event.latestGpsPoint
                    )
                }
            }
        }
    }

    private fun CareUiState.withUpdatedDependentLocation(
        dependentUserId: Long,
        latestGpsPoint: CareLatestGpsPoint
    ): CareUiState {
        if (dependents.none { dependent -> dependent.dependentUserId == dependentUserId }) {
            return this
        }

        val updatedDependents = dependents.map { dependent ->
            if (dependent.dependentUserId == dependentUserId) {
                dependent.copy(
                    latestLatitude = latestGpsPoint.latitude,
                    latestLongitude = latestGpsPoint.longitude,
                    latestRecordedAt = latestGpsPoint.recordedAt
                )
            } else {
                dependent
            }
        }

        return copy(
            dependents = updatedDependents,
            mapMarkers = updatedDependents.toCareDependentMapMarkerUiStates(),
            locationStreamErrorMessage = null
        )
    }

    private fun loadProtectedPersonDetails(
        dependentUserId: Long,
        dateKey: String
    ) {
        loadProtectedPersonRoute(
            dependentUserId = dependentUserId,
            dateKey = dateKey
        )
        loadProtectedPersonPlaces(
            dependentUserId = dependentUserId,
            dateKey = dateKey
        )
        loadProtectedPersonSummary(
            dependentUserId = dependentUserId,
            dateKey = dateKey
        )
    }

    private fun loadProtectedPersonRoute(
        dependentUserId: Long,
        dateKey: String
    ) {
        viewModelScope.launch {
            _uiState.update { state ->
                if (!state.isCurrentDetailRequest(dependentUserId, dateKey)) {
                    state
                } else {
                    state.copy(
                        isRouteLoading = true,
                        hasRouteLoaded = false,
                        isRouteEmpty = false,
                        routeErrorMessage = null
                    )
                }
            }

            val result = getProtectedPersonDayRouteUseCase(
                dependentUserId = dependentUserId,
                dateKey = dateKey
            )

            _uiState.update { state ->
                if (!state.isCurrentDetailRequest(dependentUserId, dateKey)) {
                    return@update state
                }

                when (result) {
                    ProtectedPersonDayRouteResult.Empty -> state.copy(
                        isRouteLoading = false,
                        hasRouteLoaded = true,
                        isRouteEmpty = true,
                        routeErrorMessage = null
                    )

                    is ProtectedPersonDayRouteResult.Error -> state.copy(
                        isRouteLoading = false,
                        hasRouteLoaded = true,
                        isRouteEmpty = false,
                        routeErrorMessage = ProtectedPersonRouteLoadErrorMessage
                    )

                    is ProtectedPersonDayRouteResult.Success -> {
                        val route = result.routeDetail.toProtectedPersonRouteMapUiState()
                        state.copy(
                            route = route,
                            isRouteLoading = false,
                            hasRouteLoaded = true,
                            isRouteEmpty = route.mapPolylinePoints.isEmpty(),
                            routeErrorMessage = null
                        )
                    }
                }
            }
        }
    }

    private fun loadProtectedPersonPlaces(
        dependentUserId: Long,
        dateKey: String
    ) {
        viewModelScope.launch {
            _uiState.update { state ->
                if (!state.isCurrentDetailRequest(dependentUserId, dateKey)) {
                    state
                } else {
                    state.copy(
                        placeListUiState = state.placeListUiState.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    ).withProtectedPersonPlaceGuideBannerVisibility(
                        isProtectedPersonPlaceGuideBannerDismissed
                    )
                }
            }

            val result = getProtectedPersonVisitedPlacesUseCase(
                dependentUserId = dependentUserId,
                dateKey = dateKey
            )

            _uiState.update { state ->
                if (!state.isCurrentDetailRequest(dependentUserId, dateKey)) {
                    return@update state
                }

                when (result) {
                    ProtectedPersonVisitedPlaceResult.Empty -> state.copy(
                        placeListUiState = ProtectedPersonPlaceListUiState(
                            hasLoaded = true
                        )
                    ).withProtectedPersonPlaceGuideBannerVisibility(
                        isProtectedPersonPlaceGuideBannerDismissed
                    )

                    is ProtectedPersonVisitedPlaceResult.Error -> state.copy(
                        placeListUiState = state.placeListUiState.copy(
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = ProtectedPersonPlacesLoadErrorMessage
                        )
                    ).withProtectedPersonPlaceGuideBannerVisibility(
                        isProtectedPersonPlaceGuideBannerDismissed
                    )

                    is ProtectedPersonVisitedPlaceResult.Success -> state.copy(
                        placeListUiState = result.placeList.toProtectedPersonPlaceListUiState()
                    ).withProtectedPersonPlaceGuideBannerVisibility(
                        isProtectedPersonPlaceGuideBannerDismissed
                    )
                }
            }
        }
    }

    private fun loadProtectedPersonSummary(
        dependentUserId: Long,
        dateKey: String
    ) {
        viewModelScope.launch {
            _uiState.update { state ->
                if (!state.isCurrentDetailRequest(dependentUserId, dateKey)) {
                    state
                } else {
                    state.copy(
                        summaryUiState = state.summaryUiState.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    )
                }
            }

            val result = getProtectedPersonDaySummaryUseCase(
                dependentUserId = dependentUserId,
                dateKey = dateKey
            )

            _uiState.update { state ->
                if (!state.isCurrentDetailRequest(dependentUserId, dateKey)) {
                    return@update state
                }

                when (result) {
                    ProtectedPersonDaySummaryResult.Empty -> state.copy(
                        summaryUiState = ProtectedPersonSummaryUiState(
                            hasLoaded = true
                        )
                    )

                    is ProtectedPersonDaySummaryResult.Error -> state.copy(
                        summaryUiState = state.summaryUiState.copy(
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = ProtectedPersonSummaryLoadErrorMessage
                        )
                    )

                    is ProtectedPersonDaySummaryResult.Success -> state.copy(
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

    private fun CareUiState.isCurrentDetailRequest(
        dependentUserId: Long,
        dateKey: String
    ): Boolean {
        return selectedDependentUserId == dependentUserId && selectedDateKey == dateKey
    }

    private fun CareUiState.withProtectedPersonPlaceGuideBannerVisibility(
        isDismissed: Boolean
    ): CareUiState {
        return copy(
            placeListUiState = placeListUiState.copy(
                isPlaceGuideBannerVisible = selectedDependentUserId != null && !isDismissed
            )
        )
    }

    private companion object {
        const val CareDependentsLoadErrorMessage = "Failed to load care dependents"
        const val ProtectedPersonRouteLoadErrorMessage =
            "Failed to load protected person route"
        const val ProtectedPersonPlacesLoadErrorMessage =
            "Failed to load protected person places"
        const val ProtectedPersonSummaryLoadErrorMessage =
            "Failed to load protected person summary"
        const val CareInviteLinkCreateErrorMessage = "Failed to create invite link"
        const val CareLocationStreamErrorMessage =
            "Failed to connect real-time location stream"
    }
}

class CareViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CareViewModel::class.java)) {
            return CareViewModel(
                getCareDependentsUseCase = appContainer.getCareDependentsUseCase,
                getProtectedPersonDayRouteUseCase =
                    appContainer.getProtectedPersonDayRouteUseCase,
                getProtectedPersonVisitedPlacesUseCase =
                    appContainer.getProtectedPersonVisitedPlacesUseCase,
                getProtectedPersonDaySummaryUseCase =
                    appContainer.getProtectedPersonDaySummaryUseCase,
                createCareRelationshipInviteLinkUseCase =
                    appContainer.createCareRelationshipInviteLinkUseCase,
                observeCareDependentLocationStreamUseCase =
                    appContainer.observeCareDependentLocationStreamUseCase,
                careGuideRepository = appContainer.careGuideRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private class InMemoryCareGuideRepository : CareGuideRepository {
    private val dismissed = MutableStateFlow(false)

    override val isProtectedPersonPlaceGuideBannerDismissed: Flow<Boolean> = dismissed

    override suspend fun dismissProtectedPersonPlaceGuideBanner() {
        dismissed.value = true
    }
}

private fun defaultKstTodayDateKey(): String {
    return LocalDate.now(ZoneId.of("Asia/Seoul")).toString()
}
