package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.main.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.main.presentation.state.SelectedDayRouteUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val permissionChecker: LocationPermissionChecker,
    private val dayRouteRepository: DayRouteRepository
) : ViewModel() {

    private val initialDateKey = todayDateKey()
    private val _uiState = MutableStateFlow(
        MainUiState(
            selectedDateKey = initialDateKey,
            selectedRoute = SelectedDayRouteUiState(dateKey = initialDateKey)
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
        loadDayRoute(initialDateKey)
    }

    fun refreshPermissionState() {
        val permissionState = when {
            permissionChecker.isBackgroundAlwaysGranted() -> LocationPermissionUiState.ALWAYS
            permissionChecker.isForegroundGranted() -> LocationPermissionUiState.FOREGROUND_ONLY
            else -> LocationPermissionUiState.DENIED
        }

        _uiState.update { currentState ->
            if (permissionState == LocationPermissionUiState.DENIED) {
                currentState.copy(
                    permissionState = permissionState,
                    currentLocation = null,
                    hasCenteredOnCurrentLocation = false
                )
            } else {
                currentState.copy(permissionState = permissionState)
            }
        }
    }

    fun updateCurrentLocation(location: MainCoordinateUiState) {
        _uiState.update { currentState ->
            currentState.copy(currentLocation = location)
        }
    }

    fun markInitialCameraCentered() {
        _uiState.update { currentState ->
            currentState.copy(hasCenteredOnCurrentLocation = true)
        }
    }

    fun selectDate(dateKey: String) {
        loadDayRoute(dateKey)
    }

    private fun loadDayRoute(dateKey: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    selectedDateKey = dateKey,
                    selectedRoute = SelectedDayRouteUiState(dateKey = dateKey),
                    isRouteLoading = true,
                    isRouteEmpty = false,
                    routeEmptyMessage = null,
                    routeErrorMessage = null,
                    hasCenteredOnCurrentLocation = false
                )
            }

            when (val result = dayRouteRepository.fetchRemoteDayRoute(dateKey)) {
                is RemoteDayRouteResult.Success -> {
                    val routeDetail = result.routeDetail
                    _uiState.update { currentState ->
                        currentState.copy(
                            selectedDateKey = routeDetail.dateKey,
                            selectedRoute = routeDetail.toUiState(),
                            isRouteLoading = false,
                            isRouteEmpty = false,
                            routeEmptyMessage = null,
                            routeErrorMessage = null
                        )
                    }
                }
                RemoteDayRouteResult.Empty -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            selectedDateKey = dateKey,
                            selectedRoute = SelectedDayRouteUiState(dateKey = dateKey),
                            isRouteLoading = false,
                            isRouteEmpty = true,
                            routeEmptyMessage = "No route data exists for this day.",
                            routeErrorMessage = null
                        )
                    }
                }
                is RemoteDayRouteResult.Error -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            selectedDateKey = dateKey,
                            selectedRoute = SelectedDayRouteUiState(dateKey = dateKey),
                            isRouteLoading = false,
                            isRouteEmpty = false,
                            routeEmptyMessage = null,
                            routeErrorMessage = "Failed to load the selected route."
                        )
                    }
                }
            }
        }
    }

    private fun todayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
    }
}

private fun DayRouteDetail.toUiState(): SelectedDayRouteUiState {
    return SelectedDayRouteUiState(
        dateKey = dateKey,
        polylinePoints = polylinePoints.map(RoutePoint::toUiState),
        totalDistanceKm = totalDistanceKm,
        pathPointCount = pathPointCount,
        places = places.map(DayRoutePlace::toUiState)
    )
}

private fun RoutePoint.toUiState(): MainCoordinateUiState {
    return MainCoordinateUiState(
        latitude = latitude,
        longitude = longitude
    )
}

private fun DayRoutePlace.toUiState(): PlaceMarkerUiState {
    return PlaceMarkerUiState(
        placeId = placeId,
        placeName = placeName,
        latitude = latitude,
        longitude = longitude,
        orderIndex = orderIndex
    )
}

class MainViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                permissionChecker = appContainer.permissionChecker,
                dayRouteRepository = appContainer.dayRouteRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
