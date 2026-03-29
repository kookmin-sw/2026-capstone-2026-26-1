package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModel
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModelFactory

@Composable
fun MainRoute(
    // 1. Route가 ViewModel을 생성
    // viewModel을 전달하지 않으면, MainViewModelFactory를 사용하는 ViewModel을 생성
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.appContainer)
    )
) {
    // AppContainer에서 필요한 의존성을 변수에 저장
    val appContainer = LocalContext.current.appContainer
    val lifecycleOwner = LocalLifecycleOwner.current
    val locationTracker = appContainer.currentLocationTracker
    val startLocationTracking = appContainer.startLocationTrackingUseCase
    val stopLocationTracking = appContainer.stopLocationTrackingUseCase

    // ViewModel의 상태를 구독하는 uiState 변수
    val uiState by viewModel.uiState.collectAsState()

    // 화면이 다시 활성화될 때마다(ON_RESUME) 권한 상태 재확인
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 권한이 있고 좌표가 비어 있으면 현재 위치를 먼저 반영한다.
    LaunchedEffect(uiState.permissionState, uiState.currentLocation) {
        val canReceiveLocationUpdates =
            uiState.permissionState == LocationPermissionUiState.ALWAYS ||
                    uiState.permissionState == LocationPermissionUiState.FOREGROUND_ONLY

        if (canReceiveLocationUpdates && uiState.currentLocation == null) {
            locationTracker.getCurrentLocation()?.let { trackedLocation ->
                viewModel.updateCurrentLocation(trackedLocation.toMainCoordinateUiState())
            }
        }
    }

    // 현재 권한 상태에 따라 백그라운드 추적을 시작하거나 중지한다.
    LaunchedEffect(uiState.permissionState) {
        if (uiState.permissionState == LocationPermissionUiState.ALWAYS) {
            startLocationTracking()
        } else {
            stopLocationTracking()
        }
    }

    // 위치 접근이 허용된 동안에만 실시간 위치 업데이트를 구독한다.
    DisposableEffect(uiState.permissionState, locationTracker) {
        val canReceiveLocationUpdates =
            uiState.permissionState == LocationPermissionUiState.ALWAYS ||
                    uiState.permissionState == LocationPermissionUiState.FOREGROUND_ONLY

        if (!canReceiveLocationUpdates) {
            onDispose { }
        } else {
            val trackingSession = locationTracker.startLocationUpdates { trackedLocation ->
                viewModel.updateCurrentLocation(trackedLocation.toMainCoordinateUiState())
            }

            onDispose {
                trackingSession.stop()
            }
        }
    }

    // 상위 composable 함수(route)에서
    // 하위 composable 함수(screen) 을 호출 => UI tree 구성됨
    MainScreen(
        uiState = uiState,
        onInitialCameraCentered = viewModel::markInitialCameraCentered,
        onDateSelected = viewModel::selectDate,
        onRetryRoute = viewModel::retrySelectedDate
    )
}

private fun TrackedLocation.toMainCoordinateUiState(): MainCoordinateUiState {
    return MainCoordinateUiState(
        latitude = latitude,
        longitude = longitude
    )
}
