package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.CareRelationshipInviteApi
import com.example.passedpath.feature.care.data.remote.dto.CareRelationshipInviteAcceptRequestDto
import com.example.passedpath.feature.care.data.remote.dto.CareRelationshipInviteLinkResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CareRelationshipInviteRepositoryImplTest {

    @Test
    fun `createInviteLink fetches and maps invite link`() = runTest {
        val fakeApi = FakeCareRelationshipInviteApi(
            createResponse = CareRelationshipInviteLinkResponseDto(
                inviteLink = "https://passedpath.site/care-relationship/invite?inviteCode=T5rfCFFy9j"
            )
        )
        val repository = CareRelationshipInviteRepositoryImpl(
            careRelationshipInviteApi = fakeApi
        )

        val result = repository.createInviteLink()

        assertEquals(1, fakeApi.createRequestCount)
        assertEquals(
            "https://passedpath.site/care-relationship/invite?inviteCode=T5rfCFFy9j",
            result.inviteLink
        )
    }

    @Test
    fun `createInviteLink propagates api exception`() = runTest {
        val expected = IllegalStateException("network failed")
        val repository = CareRelationshipInviteRepositoryImpl(
            careRelationshipInviteApi = FakeCareRelationshipInviteApi(createThrowable = expected)
        )

        try {
            repository.createInviteLink()
            fail("Expected exception to be thrown")
        } catch (actual: IllegalStateException) {
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `acceptInvite sends trimmed invite code request`() = runTest {
        val fakeApi = FakeCareRelationshipInviteApi(
            acceptResponse = Response.success(Unit)
        )
        val repository = CareRelationshipInviteRepositoryImpl(
            careRelationshipInviteApi = fakeApi
        )

        repository.acceptInvite(" T5rfCFFy9j ")

        assertEquals(1, fakeApi.acceptRequestCount)
        assertEquals("T5rfCFFy9j", fakeApi.receivedAcceptRequest?.inviteCode)
    }

    @Test
    fun `acceptInvite completes on successful response`() = runTest {
        val fakeApi = FakeCareRelationshipInviteApi(
            acceptResponse = Response.success(Unit)
        )
        val repository = CareRelationshipInviteRepositoryImpl(
            careRelationshipInviteApi = fakeApi
        )

        repository.acceptInvite("T5rfCFFy9j")

        assertEquals(1, fakeApi.acceptRequestCount)
    }

    @Test
    fun `acceptInvite throws HttpException on failed response`() = runTest {
        val fakeApi = FakeCareRelationshipInviteApi(
            acceptResponse = Response.error(
                400,
                """{"code":"INVALID_INVITE_CODE"}"""
                    .toResponseBody("application/json".toMediaType())
            )
        )
        val repository = CareRelationshipInviteRepositoryImpl(
            careRelationshipInviteApi = fakeApi
        )

        try {
            repository.acceptInvite("T5rfCFFy9j")
            fail("Expected HttpException to be thrown")
        } catch (actual: HttpException) {
            assertEquals(400, actual.code())
        }
    }

    @Test
    fun `acceptInvite propagates api exception`() = runTest {
        val expected = IllegalStateException("network failed")
        val repository = CareRelationshipInviteRepositoryImpl(
            careRelationshipInviteApi = FakeCareRelationshipInviteApi(
                acceptThrowable = expected
            )
        )

        try {
            repository.acceptInvite("T5rfCFFy9j")
            fail("Expected exception to be thrown")
        } catch (actual: IllegalStateException) {
            assertEquals(expected, actual)
        }
    }

    private class FakeCareRelationshipInviteApi(
        private val createResponse: CareRelationshipInviteLinkResponseDto =
            CareRelationshipInviteLinkResponseDto(inviteLink = "https://example.com/invite"),
        private val createThrowable: Throwable? = null,
        private val acceptResponse: Response<Unit> = Response.success(Unit),
        private val acceptThrowable: Throwable? = null
    ) : CareRelationshipInviteApi {
        var createRequestCount: Int = 0
        var acceptRequestCount: Int = 0
        var receivedAcceptRequest: CareRelationshipInviteAcceptRequestDto? = null

        override suspend fun createInviteLink(): CareRelationshipInviteLinkResponseDto {
            createRequestCount++
            createThrowable?.let { throw it }
            return createResponse
        }

        override suspend fun acceptInvite(
            request: CareRelationshipInviteAcceptRequestDto
        ): Response<Unit> {
            acceptRequestCount++
            receivedAcceptRequest = request
            acceptThrowable?.let { throw it }
            return acceptResponse
        }
    }
}
