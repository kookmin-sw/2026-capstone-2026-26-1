package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.main.data.manager.CurrentLocationProvider
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModel
import com.example.passedpath.feature.main.presentation.viewmodel.MainViewModelFactory

@Composable
fun MainRoute(
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentLocationProvider = remember(context) {
        CurrentLocationProvider(context)
    }
    val uiState by viewModel.uiState.collectAsState()

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

    LaunchedEffect(uiState.permissionState, uiState.currentLocation) {
        val canFetchCurrentLocation =
            uiState.permissionState == LocationPermissionUiState.ALWAYS ||
                uiState.permissionState == LocationPermissionUiState.FOREGROUND_ONLY

        if (canFetchCurrentLocation && uiState.currentLocation == null) {
            currentLocationProvider.getCurrentLocation()?.let(viewModel::updateCurrentLocation)
        }
    }

    MainScreen(
        uiState = uiState,
        onInitialCameraCentered = viewModel::markInitialCameraCentered
    )
}
