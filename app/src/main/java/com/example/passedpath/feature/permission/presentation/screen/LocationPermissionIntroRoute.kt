package com.example.passedpath.feature.permission.presentation.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.permission.presentation.viewmodel.PermissionEffect
import com.example.passedpath.feature.permission.presentation.viewmodel.PermissionViewModel
import com.example.passedpath.feature.permission.presentation.viewmodel.PermissionViewModelFactory
import com.example.passedpath.util.AppSettingsNavigator
import kotlinx.coroutines.launch

@Composable
fun LocationPermissionIntroRoute(
    onPermissionResolved: () -> Unit,
    viewModel: PermissionViewModel = viewModel(
        factory = PermissionViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val returnedFromSettings = remember { mutableStateOf(false) }

    val foregroundPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            coroutineScope.launch {
                viewModel.onForegroundPermissionResult()
            }
        }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PermissionEffect.RequestForegroundPermission -> {
                    foregroundPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }

                PermissionEffect.OpenAppSettings -> {
                    returnedFromSettings.value = true
                    AppSettingsNavigator.openAppSettings(context)
                }

                PermissionEffect.NavigateNext -> {
                    onPermissionResolved()
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && returnedFromSettings.value) {
                returnedFromSettings.value = false
                coroutineScope.launch {
                    viewModel.onReturnedFromSettings()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LocationPermissionIntroScreen(
        showSettingsDialog = uiState.showSettingsDialog,
        onContinueClick = {
            coroutineScope.launch {
                viewModel.onContinueClick()
            }
        },
        onDialogConfirm = {
            coroutineScope.launch {
                viewModel.onSettingsConfirm()
            }
        },
        onDialogDismiss = {
            coroutineScope.launch {
                viewModel.onSettingsDismiss()
            }
        }
    )
}
