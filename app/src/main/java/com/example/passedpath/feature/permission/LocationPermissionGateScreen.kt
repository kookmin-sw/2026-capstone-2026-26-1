package com.example.passedpath.feature.permission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.passedpath.navigation.NavRoute
import com.example.passedpath.ui.PermissionSettingDialog
import com.example.passedpath.util.AppSettingsNavigator

// Gate 전용 상태
private enum class GatePhase {
    CHECKING,
    NEED_SETTINGS
}

@Composable
fun LocationPermissionGateScreen(
    navController: NavController,
    viewModel: LocationPermissionViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var phase by remember { mutableStateOf(GatePhase.CHECKING) }
    var waitingForSettings by remember { mutableStateOf(false) }

    // 포그라운드 권한 요청 런처
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* 결과 해석 없음 */ }

    // Gate 진입 시 최초 체크
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        if (LocationPermissionGate.isBackgroundAlwaysGranted(context)) {
            navController.navigate(NavRoute.MAIN) {
                popUpTo(NavRoute.PERMISSION_INTRO) { inclusive = true }
            }
        } else {
            phase = GatePhase.NEED_SETTINGS
        }
    }

    // 설정 앱 복귀 감지
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && waitingForSettings) {
                waitingForSettings = false

                if (LocationPermissionGate.isBackgroundAlwaysGranted(context)) {
                    navController.navigate(NavRoute.MAIN) {
                        popUpTo(NavRoute.PERMISSION_INTRO) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoute.MAIN) {
                        popUpTo(NavRoute.PERMISSION_INTRO) { inclusive = true }
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 설정 이동 다이얼로그
    if (phase == GatePhase.NEED_SETTINGS) {
        PermissionSettingDialog(
            onConfirm = {
                waitingForSettings = true
                AppSettingsNavigator.openAppSettings(context)
            },
            onDismiss = {
                navController.navigate(NavRoute.MAIN) {
                    popUpTo(NavRoute.PERMISSION_INTRO) { inclusive = true }
                }
            }
        )
    }
}
