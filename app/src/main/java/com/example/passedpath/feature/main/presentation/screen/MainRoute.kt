package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModel
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModelFactory
import com.example.passedpath.util.AppSettingsNavigator

@Composable
fun MainRoute(
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current
    val appContainer = context.appContainer
    val uiState by viewModel.uiState.collectAsState()

    MainRouteEffects(
        permissionState = uiState.permissionState,
        isLocationServiceEnabled = uiState.isLocationServiceEnabled,
        currentLocation = uiState.currentLocation,
        isTrackingActive = uiState.isTrackingActive,
        onRefreshPermissionState = viewModel::refreshPermissionState,
        onRefreshLocationServiceState = viewModel::refreshLocationServiceState,
        onCurrentLocationUpdated = viewModel::updateCurrentLocation,
        locationTracker = appContainer.currentLocationTracker,
        trackingServiceStateReader = appContainer.locationTrackingServiceStateReader,
        startLocationTracking = { persistUserPreference ->
            appContainer.startLocationTrackingUseCase(persistUserPreference)
        },
        stopLocationTracking = { persistUserPreference ->
            appContainer.stopLocationTrackingUseCase(persistUserPreference)
        }
    )

    MainScreen(
        uiState = uiState,
        onInitialCameraCentered = viewModel::markInitialCameraCentered,
        onDateSelected = viewModel::selectDate,
        onRouteAction = viewModel::handleRouteAction,
        onTrackingPermissionDialogConfirm = {
            viewModel.dismissTrackingPermissionDialog()
            AppSettingsNavigator.openAppSettings(context)
        },
        onTrackingPermissionDialogDismiss = viewModel::dismissTrackingPermissionDialog,
        onPermissionBannerConfirm = {
            when {
                uiState.permissionState != LocationPermissionUiState.ALWAYS -> {
                    AppSettingsNavigator.openAppSettings(context)
                }
                !uiState.isLocationServiceEnabled -> {
                    AppSettingsNavigator.openLocationSettings(context)
                }
            }
        }
    )
}

