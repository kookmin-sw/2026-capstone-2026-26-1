package com.example.passedpath.feature.care.presentation.deeplink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CareInviteDeepLinkParserTest {
    @Test
    fun `extractInviteCode returns code from valid invite link`() {
        val inviteCode = CareInviteDeepLinkParser.extractInviteCode(
            "https://passedpath.site/care-relationship/invite?inviteCode=T5rfCFFy9j"
        )

        assertEquals("T5rfCFFy9j", inviteCode)
    }

    @Test
    fun `extractInviteCode trims decoded invite code`() {
        val inviteCode = CareInviteDeepLinkParser.extractInviteCode(
            "https://passedpath.site/care-relationship/invite?inviteCode=%20abc123%20"
        )

        assertEquals("abc123", inviteCode)
    }

    @Test
    fun `extractInviteCode ignores invalid host path and blank code`() {
        assertNull(
            CareInviteDeepLinkParser.extractInviteCode(
                "https://example.com/care-relationship/invite?inviteCode=abc"
            )
        )
        assertNull(
            CareInviteDeepLinkParser.extractInviteCode(
                "https://passedpath.site/other/invite?inviteCode=abc"
            )
        )
        assertNull(
            CareInviteDeepLinkParser.extractInviteCode(
                "https://passedpath.site/care-relationship/invite?inviteCode=%20%20"
            )
        )
    }
}
