package com.example.passedpath.feature.main.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _permissionUiState =
        MutableStateFlow(LocationPermissionUiState.LIMITED)
    val permissionUiState: StateFlow<LocationPermissionUiState> = _permissionUiState

    // Main 진입 시 위치 권한 상태 확인
    fun checkPermission(context: Context) {
        val isAlwaysGranted =
            LocationPermissionGate.isBackgroundAlwaysGranted(context)

        _permissionUiState.value =
            if (isAlwaysGranted) {
                LocationPermissionUiState.FULL
            } else {
                LocationPermissionUiState.LIMITED
            }
    }
}
