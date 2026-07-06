package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareRelationshipInviteAcceptRequestDto
import com.example.passedpath.feature.care.data.remote.dto.CareRelationshipInviteLinkResponseDto
import com.example.passedpath.feature.care.domain.model.CareRelationshipInviteLink

internal fun CareRelationshipInviteLinkResponseDto.toCareRelationshipInviteLink(): CareRelationshipInviteLink {
    val resolvedInviteLink = inviteLink?.trim()?.takeIf(String::isNotEmpty)
        ?: error("inviteLink is missing in care relationship invite link response")

    return CareRelationshipInviteLink(inviteLink = resolvedInviteLink)
}

internal fun String.toCareRelationshipInviteAcceptRequestDto(): CareRelationshipInviteAcceptRequestDto {
    val resolvedInviteCode = trim().takeIf(String::isNotEmpty)
        ?: throw IllegalArgumentException("inviteCode must not be blank")

    return CareRelationshipInviteAcceptRequestDto(inviteCode = resolvedInviteCode)
}
