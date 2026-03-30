package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.state.MainUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.route.presentation.mapper.createInitialRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createLoadingRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastEmptyRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastErrorRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createTodayEmptyRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createTodayRouteMode
import com.example.passedpath.feature.route.presentation.mapper.toSelectedDayRouteUiState
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
            routeModeUiState = createInitialRouteMode(
                dateKey = initialDateKey,
                isToday = isToday(initialDateKey)
            )
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
                        createTodayEmptyRouteMode(dateKey)
                    } else {
                        createTodayRouteMode(route = dailyPath.toSelectedDayRouteUiState())
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
                        routeModeUiState = createPastRouteMode(
                            route = routeDetail.toSelectedDayRouteUiState()
                        )
                    )
                }
            }
            RemoteDayRouteResult.Empty -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedDateKey = dateKey,
                        routeModeUiState = createPastEmptyRouteMode(dateKey)
                    )
                }
            }
            is RemoteDayRouteResult.Error -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedDateKey = dateKey,
                        routeModeUiState = createPastErrorRouteMode(dateKey)
                    )
                }
            }
        }
    }

    private fun setRouteLoadingState(dateKey: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDateKey = dateKey,
                routeModeUiState = createLoadingRouteMode(
                    dateKey = dateKey,
                    isToday = isToday(dateKey)
                ),
                hasCenteredOnCurrentLocation = false
            )
        }
    }

    private fun isToday(dateKey: String): Boolean {
        return dateKey == todayDateKeyProvider()
    }
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
