package com.example.passedpath.feature.mypage.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.passedpath.R
import com.example.passedpath.app.AppContainer
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.feature.auth.presentation.state.AuthEvent
import com.example.passedpath.feature.main.data.repository.TestRepository
import com.example.passedpath.ui.state.AsyncUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MyPageViewModel(
    private val testRepository: TestRepository,
    private val authSessionStorage: AuthSessionStorage
) : ViewModel() {

    private val _testResult = MutableStateFlow<AsyncUiState<String>>(AsyncUiState.Idle)
    val testResult: StateFlow<AsyncUiState<String>> = _testResult

    fun testApi() {
        viewModelScope.launch {
            _testResult.value = AsyncUiState.Loading
            try {
                val result = testRepository.test()
                _testResult.value = AsyncUiState.Success(result)
                Log.d("TEST", "API success: $result")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _testResult.value = AsyncUiState.Error(R.string.main_test_error_http)
                Log.e("TEST", "HTTP ${e.code()} error")
                Log.e("TEST", "Error body: $errorBody")
            } catch (e: Exception) {
                _testResult.value = AsyncUiState.Error(R.string.main_test_error_generic)
                Log.e("TEST", "Unexpected error", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authSessionStorage.clear()
            AuthEvent.logoutEvent.emit(Unit)
        }
    }
}

class MyPageViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyPageViewModel(
                testRepository = appContainer.testRepository,
                authSessionStorage = appContainer.authSessionStorage
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
