package com.example.passedpath.feature.auth

import kotlinx.coroutines.flow.MutableSharedFlow

object AuthEvent {
    val logoutEvent = MutableSharedFlow<Unit>()
}