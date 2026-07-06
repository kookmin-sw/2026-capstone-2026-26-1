package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareRelationshipInviteLinkResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CareRelationshipInviteRemoteMapperTest {

    @Test
    fun `toCareRelationshipInviteLink maps invite link`() {
        val response = CareRelationshipInviteLinkResponseDto(
            inviteLink = " https://passedpath.site/care-relationship/invite?inviteCode=T5rfCFFy9j "
        )

        val result = response.toCareRelationshipInviteLink()

        assertEquals(
            "https://passedpath.site/care-relationship/invite?inviteCode=T5rfCFFy9j",
            result.inviteLink
        )
    }

    @Test
    fun `toCareRelationshipInviteLink throws when invite link is missing`() {
        listOf(null, "", " ").forEach { inviteLink ->
            try {
                CareRelationshipInviteLinkResponseDto(inviteLink = inviteLink)
                    .toCareRelationshipInviteLink()
                fail("Expected exception for inviteLink=$inviteLink")
            } catch (actual: IllegalStateException) {
                assertTrue(actual.message.orEmpty().contains("inviteLink"))
            }
        }
    }

    @Test
    fun `toCareRelationshipInviteAcceptRequestDto trims invite code`() {
        val result = " T5rfCFFy9j ".toCareRelationshipInviteAcceptRequestDto()

        assertEquals("T5rfCFFy9j", result.inviteCode)
    }

    @Test
    fun `toCareRelationshipInviteAcceptRequestDto throws when invite code is blank`() {
        listOf("", " ").forEach { inviteCode ->
            try {
                inviteCode.toCareRelationshipInviteAcceptRequestDto()
                fail("Expected exception for inviteCode=$inviteCode")
            } catch (actual: IllegalArgumentException) {
                assertTrue(actual.message.orEmpty().contains("inviteCode"))
            }
        }
    }
}
