package com.example.passedpath.feature.care.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class CareInviteAcceptUiState(
    val isVisible: Boolean = false,
    val inviteCode: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successEventId: Long = 0L
)
