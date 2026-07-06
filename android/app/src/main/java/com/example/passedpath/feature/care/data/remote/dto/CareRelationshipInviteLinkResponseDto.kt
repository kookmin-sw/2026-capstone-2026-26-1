package com.example.passedpath.feature.care.data.remote.dto

data class CareRelationshipInviteLinkResponseDto(
    val inviteLink: String?
)

data class CareRelationshipInviteAcceptRequestDto(
    val inviteCode: String
)
