package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.main.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.main.presentation.state.SelectedDayRouteUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val locationPermissionStatusReader: LocationPermissionStatusReader,
    private val dayRouteRepository: DayRouteRepository,
    initialDateKeyProvider: () -> String = ::todayDateKey,
    private val todayDateKeyProvider: () -> String = ::todayDateKey
) : ViewModel() {

    private val initialDateKey = initialDateKeyProvider()
    private var routeLoadJob: Job? = null

    private val _uiState = MutableStateFlow(
        MainUiState(
            selectedDateKey = initialDateKey,
            routeModeUiState = createInitialRouteMode(initialDateKey)
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
        loadDayRoute(initialDateKey)
    }

    fun refreshPermissionState() {
        val permissionState = when {
            locationPermissionStatusReader.isBackgroundAlwaysGranted() -> LocationPermissionUiState.ALWAYS
            locationPermissionStatusReader.isForegroundGranted() -> LocationPermissionUiState.FOREGROUND_ONLY
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

    fun retrySelectedDate() {
        loadDayRoute(_uiState.value.selectedDateKey)
    }

    private fun loadDayRoute(dateKey: String) {
        routeLoadJob?.cancel()
        routeLoadJob = viewModelScope.launch {
            setRouteLoadingState(dateKey)

            if (isToday(dateKey)) {
                observeTodayRoute(dateKey)
            } else {
                loadPastRoute(dateKey)
            }
        }
    }

    private suspend fun observeTodayRoute(dateKey: String) {
        dayRouteRepository.observeLocalDayRoute(dateKey).collect { dailyPath ->
            _uiState.update { currentState ->
                currentState.copy(
                    selectedDateKey = dateKey,
                    routeModeUiState = if (dailyPath == null) {
                        createTodayRouteMode(
                            route = SelectedDayRouteUiState(dateKey = dateKey),
                            isRouteEmpty = true,
                            routeEmptyMessage = "No route data exists for this day."
                        )
                    } else {
                        createTodayRouteMode(route = dailyPath.toUiState())
                    }
                )
            }
        }
    }

    private suspend fun loadPastRoute(dateKey: String) {
        when (val result = dayRouteRepository.fetchRemoteDayRoute(dateKey)) {
            is RemoteDayRouteResult.Success -> {
                val routeDetail = result.routeDetail
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedDateKey = routeDetail.dateKey,
                        routeModeUiState = createPastRouteMode(route = routeDetail.toUiState())
                    )
                }
            }
            RemoteDayRouteResult.Empty -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedDateKey = dateKey,
                        routeModeUiState = createPastRouteMode(
                            route = SelectedDayRouteUiState(dateKey = dateKey),
                            isRouteEmpty = true,
                            routeEmptyMessage = "No route data exists for this day."
                        )
                    )
                }
            }
            is RemoteDayRouteResult.Error -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedDateKey = dateKey,
                        routeModeUiState = createPastRouteMode(
                            route = SelectedDayRouteUiState(dateKey = dateKey),
                            routeErrorMessage = "Failed to load the selected route."
                        )
                    )
                }
            }
        }
    }

    private fun setRouteLoadingState(dateKey: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDateKey = dateKey,
                routeModeUiState = if (isToday(dateKey)) {
                    createTodayRouteMode(
                        route = SelectedDayRouteUiState(dateKey = dateKey),
                        isRouteLoading = true
                    )
                } else {
                    createPastRouteMode(
                        route = SelectedDayRouteUiState(dateKey = dateKey),
                        isRouteLoading = true
                    )
                },
                hasCenteredOnCurrentLocation = false
            )
        }
    }

    private fun createInitialRouteMode(dateKey: String): MainRouteModeUiState {
        val route = SelectedDayRouteUiState(dateKey = dateKey)
        return if (isToday(dateKey)) {
            createTodayRouteMode(route = route)
        } else {
            createPastRouteMode(route = route)
        }
    }

    private fun createTodayRouteMode(
        route: SelectedDayRouteUiState,
        isRouteLoading: Boolean = false,
        isRouteEmpty: Boolean = false,
        routeEmptyMessage: String? = null,
        routeErrorMessage: String? = null
    ): MainRouteModeUiState.Today {
        return MainRouteModeUiState.Today(
            route = route,
            isRouteLoading = isRouteLoading,
            isRouteEmpty = isRouteEmpty,
            routeEmptyMessage = routeEmptyMessage,
            routeErrorMessage = routeErrorMessage
        )
    }

    private fun createPastRouteMode(
        route: SelectedDayRouteUiState,
        isRouteLoading: Boolean = false,
        isRouteEmpty: Boolean = false,
        routeEmptyMessage: String? = null,
        routeErrorMessage: String? = null
    ): MainRouteModeUiState.Past {
        return MainRouteModeUiState.Past(
            route = route,
            isRouteLoading = isRouteLoading,
            isRouteEmpty = isRouteEmpty,
            routeEmptyMessage = routeEmptyMessage,
            routeErrorMessage = routeErrorMessage
        )
    }

    private fun isToday(dateKey: String): Boolean {
        return dateKey == todayDateKeyProvider()
    }
}

private fun DailyPath.toUiState(): SelectedDayRouteUiState {
    return SelectedDayRouteUiState(
        dateKey = dateKey,
        polylinePoints = points.map(TrackedLocation::toUiState),
        totalDistanceKm = totalDistanceMeters / 1000.0,
        pathPointCount = pathPointCount,
        places = emptyList()
    )
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

private fun TrackedLocation.toUiState(): MainCoordinateUiState {
    return MainCoordinateUiState(
        latitude = latitude,
        longitude = longitude
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

private fun todayDateKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
}

class MainViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                locationPermissionStatusReader = appContainer.locationPermissionStatusReader,
                dayRouteRepository = appContainer.dayRouteRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
