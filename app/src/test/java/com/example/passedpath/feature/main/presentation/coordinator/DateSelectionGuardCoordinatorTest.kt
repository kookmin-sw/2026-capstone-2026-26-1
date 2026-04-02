package com.example.passedpath.feature.main.presentation.coordinator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DateSelectionGuardCoordinatorTest {

    @Test
    fun `requestDateSelection proceeds immediately when there are no unsaved changes`() {
        val coordinator = DateSelectionGuardCoordinator()

        val decision = coordinator.requestDateSelection(
            currentDateKey = "2026-04-03",
            targetDateKey = "2026-04-02",
            hasUnsavedDayNoteChanges = false
        )

        assertEquals(DateSelectionDecision.Proceed("2026-04-02"), decision)
        assertNull(coordinator.state.value.pendingDateSelection)
    }

    @Test
    fun `requestDateSelection requires confirmation when there are unsaved changes`() {
        val coordinator = DateSelectionGuardCoordinator()

        val decision = coordinator.requestDateSelection(
            currentDateKey = "2026-04-03",
            targetDateKey = "2026-04-02",
            hasUnsavedDayNoteChanges = true
        )

        assertTrue(decision is DateSelectionDecision.RequireConfirmation)
        assertEquals("2026-04-02", coordinator.state.value.pendingDateSelection)
    }

    @Test
    fun `consumeDateSelectionAfterSave returns pending date after successful save`() {
        val coordinator = DateSelectionGuardCoordinator()
        coordinator.requestDateSelection(
            currentDateKey = "2026-04-03",
            targetDateKey = "2026-04-02",
            hasUnsavedDayNoteChanges = true
        )

        val result = coordinator.consumeDateSelectionAfterSave(
            isSubmitting = false,
            hasUnsavedDayNoteChanges = false,
            hasSaveError = false,
            hasSaveSuccessMessage = true
        )

        assertEquals("2026-04-02", result)
        assertNull(coordinator.state.value.pendingDateSelection)
    }

    @Test
    fun `consumeDateSelectionAfterSave clears pending date on save error`() {
        val coordinator = DateSelectionGuardCoordinator()
        coordinator.requestDateSelection(
            currentDateKey = "2026-04-03",
            targetDateKey = "2026-04-02",
            hasUnsavedDayNoteChanges = true
        )

        val result = coordinator.consumeDateSelectionAfterSave(
            isSubmitting = false,
            hasUnsavedDayNoteChanges = true,
            hasSaveError = true,
            hasSaveSuccessMessage = false
        )

        assertNull(result)
        assertNull(coordinator.state.value.pendingDateSelection)
    }
}
