package com.example.passedpath.feature.care.domain.repository

import com.example.passedpath.feature.care.domain.model.CareRelationshipInviteLink

interface CareRelationshipInviteRepository {
    suspend fun createInviteLink(): CareRelationshipInviteLink

    suspend fun acceptInvite(inviteCode: String)
}
