package com.example.passedpath.feature.permission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun LocationPermissionGateScreen(
    navController: NavController,
    viewModel: LocationPermissionViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // 포그라운드 위치 권한 요청을 위한 런처
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // 권한 결과는 OS가 처리하므로 별도 분기 없음
        }

    // 화면 진입 시 한 번만 권한 플로우 시작
    LaunchedEffect(Unit) {
        viewModel.checkOnAppStart(
            context = context,
            requestForegroundPermission = {
                // 포그라운드 위치 권한 팝업 요청
                permissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        )
    }

    // 권한 상태 변화에 따라 다음 화면으로 이동
    LaunchedEffect(state) {
        when (state) {

            LocationPermissionState.NORMAL -> {
                // 항상 허용 상태이므로 정상 메인 화면으로 이동
                navController.navigate("main") {
                    popUpTo("permission_gate") { inclusive = true }
                }
            }

            LocationPermissionState.NEED_SETTINGS -> {
                // 항상 허용이 아니므로 설정 이동 팝업을 보여줘야 함
                // TODO 설정 이동 팝업 표시
                // 팝업 확인 시 AppSettingNavigation.openAppSettings(context)
            }

            LocationPermissionState.LIMITED -> {
                // 설정 이후에도 항상 허용이 아니므로 제한 메인으로 이동
                navController.navigate("limited_main") {
                    popUpTo("permission_gate") { inclusive = true }
                }
            }

            null -> Unit
        }
    }
}
