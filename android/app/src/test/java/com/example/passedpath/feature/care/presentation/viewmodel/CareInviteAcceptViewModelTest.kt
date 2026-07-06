package com.example.passedpath.feature.care.presentation.viewmodel

import com.example.passedpath.feature.care.domain.model.CareRelationshipInviteLink
import com.example.passedpath.feature.care.domain.repository.CareRelationshipInviteRepository
import com.example.passedpath.feature.care.domain.usecase.AcceptCareRelationshipInviteUseCase
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CareInviteAcceptViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `openInvite stores trimmed invite code and shows dialog`() {
        val viewModel = createViewModel()

        viewModel.openInvite(" T5rfCFFy9j ")

        val state = viewModel.uiState.value
        assertTrue(state.isVisible)
        assertEquals("T5rfCFFy9j", state.inviteCode)
        assertFalse(state.isSubmitting)
        assertNull(state.errorMessage)
    }

    @Test
    fun `acceptInvite calls repository and emits success event`() = runTest {
        val repository = FakeCareRelationshipInviteRepository()
        val viewModel = createViewModel(repository)

        viewModel.openInvite(" T5rfCFFy9j ")
        viewModel.acceptInvite()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("T5rfCFFy9j"), repository.acceptedInviteCodes)
        assertFalse(state.isVisible)
        assertFalse(state.isSubmitting)
        assertNull(state.inviteCode)
        assertEquals(1L, state.successEventId)
    }

    @Test
    fun `acceptInvite exposes retryable error on failure`() = runTest {
        val repository = FakeCareRelationshipInviteRepository(
            acceptThrowable = IllegalStateException("expired")
        )
        val viewModel = createViewModel(repository)

        viewModel.openInvite("expired")
        viewModel.acceptInvite()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isVisible)
        assertEquals("expired", state.inviteCode)
        assertFalse(state.isSubmitting)
        assertNotNull(state.errorMessage)
        assertEquals(0L, state.successEventId)
    }

    @Test
    fun `dismissInvite resets dialog without consuming success event`() {
        val viewModel = createViewModel()

        viewModel.openInvite("abc")
        viewModel.dismissInvite()

        val state = viewModel.uiState.value
        assertFalse(state.isVisible)
        assertNull(state.inviteCode)
        assertEquals(0L, state.successEventId)
    }

    @Test
    fun `blank invite code is ignored`() {
        val viewModel = createViewModel()

        viewModel.openInvite("   ")

        val state = viewModel.uiState.value
        assertFalse(state.isVisible)
        assertNull(state.inviteCode)
    }

    private fun createViewModel(
        repository: CareRelationshipInviteRepository = FakeCareRelationshipInviteRepository()
    ): CareInviteAcceptViewModel {
        return CareInviteAcceptViewModel(
            acceptCareRelationshipInviteUseCase =
                AcceptCareRelationshipInviteUseCase(repository)
        )
    }

    private class FakeCareRelationshipInviteRepository(
        private val acceptThrowable: Throwable? = null
    ) : CareRelationshipInviteRepository {
        val acceptedInviteCodes = mutableListOf<String>()

        override suspend fun createInviteLink(): CareRelationshipInviteLink {
            return CareRelationshipInviteLink(
                inviteLink = "https://passedpath.site/care-relationship/invite?inviteCode=abc"
            )
        }

        override suspend fun acceptInvite(inviteCode: String) {
            acceptThrowable?.let { throw it }
            acceptedInviteCodes += inviteCode
        }
    }
}
