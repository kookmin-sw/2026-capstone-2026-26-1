package com.example.passedpath.feature.auth.presentation

import kotlinx.coroutines.flow.MutableSharedFlow

object AuthEvent {
    val logoutEvent = MutableSharedFlow<Unit>()
}
