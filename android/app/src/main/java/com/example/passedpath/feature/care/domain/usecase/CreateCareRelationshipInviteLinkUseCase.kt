package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.CareRelationshipInviteRepository

class CreateCareRelationshipInviteLinkUseCase(
    private val repository: CareRelationshipInviteRepository
) {
    suspend operator fun invoke() = repository.createInviteLink()
}
