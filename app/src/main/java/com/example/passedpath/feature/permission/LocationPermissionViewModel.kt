package com.example.passedpath.feature.permission

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// 위치 권한 플로우를 제어하는 ViewModel
class LocationPermissionViewModel : ViewModel() {

    private val _state = MutableStateFlow<LocationPermissionState?>(null)
    val state: StateFlow<LocationPermissionState?> = _state

    // 1. 앱 최초 진입 시 포그라운드 위치 권한 상태를 판단
    fun checkOnAppStart(
        context: Context,
        requestForegroundPermission: () -> Unit
    ) {
        // 포그라운드 위치가 없으면 우선 권한 요청 팝업을 시도
        if (!LocationPermissionGate.isForegroundGranted(context)) {
            requestForegroundPermission()
        }

        // 백그라운드 ALWAYS 여부만을 기준으로 최종 상태 결정
        _state.value = if (
            LocationPermissionGate.isBackgroundAlwaysGranted(context)
        ) {
            // 항상 허용 상태면 정상 메인으로 진입
            LocationPermissionState.NORMAL
        } else {
            // 항상 허용이 아니면 설정 이동이 필요
            LocationPermissionState.NEED_SETTINGS
        }
    }

    // 설정 화면에서 돌아온 후 권한을 다시 확인
    fun onReturnedFromSettings(context: Context) {
        _state.value = if (
            LocationPermissionGate.isBackgroundAlwaysGranted(context)
        ) {
            // 설정에서 항상 허용을 켰다면 정상 메인
            LocationPermissionState.NORMAL
        } else {
            // 설정 이후에도 항상 허용이 아니면 제한 메인
            LocationPermissionState.LIMITED
        }
    }
}