package com.example.passedpath.feature.care.data.remote.api

import com.example.passedpath.feature.care.data.remote.dto.CareRelationshipInviteAcceptRequestDto
import com.example.passedpath.feature.care.data.remote.dto.CareRelationshipInviteLinkResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CareRelationshipInviteApi {
    @POST("/api/care-relationship-invites/link")
    suspend fun createInviteLink(): CareRelationshipInviteLinkResponseDto

    @POST("/api/care-relationship-invites/accept")
    suspend fun acceptInvite(
        @Body request: CareRelationshipInviteAcceptRequestDto
    ): Response<Unit>
}
