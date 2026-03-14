package com.example.passedpath.feature.main.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.app.AppContainer
import com.example.passedpath.feature.main.data.repository.TestRepository
import com.example.passedpath.feature.main.presentation.state.LocationPermissionUiState
import com.example.passedpath.feature.permission.data.manager.LocationPermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val permissionChecker: LocationPermissionChecker,
    private val testRepository: TestRepository
) : ViewModel() {

    private val _permissionUiState =
        MutableStateFlow(LocationPermissionUiState.LIMITED)
    val permissionUiState: StateFlow<LocationPermissionUiState> = _permissionUiState

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult

    fun checkPermission() {
        val isAlwaysGranted = permissionChecker.isBackgroundAlwaysGranted()

        _permissionUiState.value =
            if (isAlwaysGranted) {
                LocationPermissionUiState.FULL
            } else {
                LocationPermissionUiState.LIMITED
            }
    }

    fun testApi() {
        viewModelScope.launch {
            try {
                val result = testRepository.test()
                _testResult.value = result
                Log.d("TEST", "API success: $result")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _testResult.value = "HTTP ${e.code()}"
                Log.e("TEST", "HTTP ${e.code()} error")
                Log.e("TEST", "Error body: $errorBody")
            } catch (e: Exception) {
                _testResult.value = "Request failed"
                Log.e("TEST", "Unexpected error", e)
            }
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
                permissionChecker = appContainer.permissionChecker,
                testRepository = appContainer.testRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
