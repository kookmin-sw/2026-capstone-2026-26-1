package com.example.passedpath.ui.state

import androidx.annotation.StringRes

sealed interface AsyncUiState<out T> {
    data object Idle : AsyncUiState<Nothing>
    data object Loading : AsyncUiState<Nothing>
    data class Success<T>(val data: T) : AsyncUiState<T>
    data class Error(@StringRes val messageResId: Int) : AsyncUiState<Nothing>
}
