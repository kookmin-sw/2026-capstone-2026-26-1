package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.CareRelationshipInviteRepository

class AcceptCareRelationshipInviteUseCase(
    private val repository: CareRelationshipInviteRepository
) {
    suspend operator fun invoke(inviteCode: String) {
        repository.acceptInvite(inviteCode = inviteCode)
    }
}
