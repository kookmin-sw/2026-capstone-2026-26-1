package com.example.passedpath.feature.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val permissionChecker: LocationPermissionChecker
) : ViewModel() {

    private val _permissionUiState =
        MutableStateFlow(LocationPermissionUiState.LIMITED)
    val permissionUiState: StateFlow<LocationPermissionUiState> = _permissionUiState

    init {
        checkPermission()
    }

    fun checkPermission() {
        val isAlwaysGranted = permissionChecker.isBackgroundAlwaysGranted()

        _permissionUiState.value =
            if (isAlwaysGranted) {
                LocationPermissionUiState.FULL
            } else {
                LocationPermissionUiState.LIMITED
            }
    }
}

class MainViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                permissionChecker = appContainer.permissionChecker
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
