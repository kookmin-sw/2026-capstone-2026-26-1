package com.example.passedpath.feature.daynote.presentation.viewmodel

import com.example.passedpath.feature.daynote.domain.model.DayRouteMemo
import com.example.passedpath.feature.daynote.domain.model.DayRouteTitle
import com.example.passedpath.feature.daynote.domain.repository.DayRouteMemoRepository
import com.example.passedpath.feature.daynote.domain.repository.DayRouteTitleRepository
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteMemoUseCase
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteTitleUseCase
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DayNoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `syncSelectedDay hydrates selected date title and memo`() = runTest {
        val viewModel = createViewModel()

        viewModel.syncSelectedDay(
            dateKey = "2026-04-02",
            title = "Morning route",
            memo = "Coffee stop"
        )

        val state = viewModel.uiState.value
        assertEquals("2026-04-02", state.dateKey)
        assertEquals("Morning route", state.originalTitle)
        assertEquals("Coffee stop", state.originalMemo)
        assertEquals("Morning route", state.title)
        assertEquals("Coffee stop", state.memo)
        assertFalse(state.isDirty)
    }

    @Test
    fun `syncSelectedDay replaces edited values when selected day changes`() = runTest {
        val viewModel = createViewModel()
        viewModel.syncSelectedDay(
            dateKey = "2026-04-02",
            title = "Morning route",
            memo = "Coffee stop"
        )
        viewModel.updateTitle("Edited title")
        viewModel.updateMemo("Edited memo")

        viewModel.syncSelectedDay(
            dateKey = "2026-04-03",
            title = "Evening route",
            memo = "Dinner stop"
        )

        val state = viewModel.uiState.value
        assertEquals("2026-04-03", state.dateKey)
        assertEquals("Evening route", state.originalTitle)
        assertEquals("Dinner stop", state.originalMemo)
        assertEquals("Evening route", state.title)
        assertEquals("Dinner stop", state.memo)
        assertFalse(state.isDirty)
    }

    @Test
    fun `blank-only edits are treated as unchanged after normalization`() = runTest {
        val viewModel = createViewModel()
        viewModel.syncSelectedDay(dateKey = "2026-04-02", title = "", memo = "")

        viewModel.updateTitle("   ")
        viewModel.updateMemo("  ")

        val state = viewModel.uiState.value
        assertFalse(state.isDirty)
        assertFalse(state.isSaveEnabled)
    }

    @Test
    fun `submitDayNote saves changed fields sequentially and updates originals`() = runTest {
        val callOrder = mutableListOf<String>()
        val viewModel = createViewModel(
            titleRepository = object : DayRouteTitleRepository {
                override suspend fun patchTitle(dateKey: String, title: String?): DayRouteTitle {
                    callOrder += "title:$title"
                    return DayRouteTitle(title = title)
                }
            },
            memoRepository = object : DayRouteMemoRepository {
                override suspend fun patchMemo(dateKey: String, memo: String?): DayRouteMemo {
                    callOrder += "memo:$memo"
                    return DayRouteMemo(memo = memo)
                }
            }
        )
        viewModel.syncSelectedDay(dateKey = "2026-04-02", title = "Old", memo = "Old memo")
        viewModel.updateTitle(" New title ")
        viewModel.updateMemo(" New memo ")

        viewModel.submitDayNote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("title:New title", "memo:New memo"), callOrder)
        assertEquals("New title", state.originalTitle)
        assertEquals("New memo", state.originalMemo)
        assertEquals("New title", state.title)
        assertEquals("New memo", state.memo)
        assertEquals("제목과 메모를 저장했습니다.", state.successMessage)
        assertEquals(1L, state.feedbackEventId)
    }

    @Test
    fun `submitDayNote emits snapshot patch for successful fields`() = runTest {
        val viewModel = createViewModel()
        viewModel.syncSelectedDay(dateKey = "2026-04-02", title = "Old", memo = "Old memo")
        viewModel.updateTitle("New title")
        viewModel.updateMemo("New memo")
        val patchDeferred = async { viewModel.snapshotPatch.first() }

        viewModel.submitDayNote()
        advanceUntilIdle()

        val patch = patchDeferred.await()
        assertEquals("2026-04-02", patch.dateKey)
        assertTrue(patch.shouldUpdateTitle)
        assertTrue(patch.shouldUpdateMemo)
        assertEquals("New title", patch.title)
        assertEquals("New memo", patch.memo)
    }

    @Test
    fun `submitDayNote skips unchanged field requests`() = runTest {
        val callOrder = mutableListOf<String>()
        val viewModel = createViewModel(
            titleRepository = object : DayRouteTitleRepository {
                override suspend fun patchTitle(dateKey: String, title: String?): DayRouteTitle {
                    callOrder += "title:$title"
                    return DayRouteTitle(title = title)
                }
            },
            memoRepository = object : DayRouteMemoRepository {
                override suspend fun patchMemo(dateKey: String, memo: String?): DayRouteMemo {
                    callOrder += "memo:$memo"
                    return DayRouteMemo(memo = memo)
                }
            }
        )
        viewModel.syncSelectedDay(dateKey = "2026-04-02", title = "Old", memo = "Old memo")
        viewModel.updateMemo("Updated memo")

        viewModel.submitDayNote()
        advanceUntilIdle()

        assertEquals(listOf("memo:Updated memo"), callOrder)
    }

    @Test
    fun `submitDayNote stops when title save fails before memo`() = runTest {
        val callOrder = mutableListOf<String>()
        val viewModel = createViewModel(
            titleRepository = object : DayRouteTitleRepository {
                override suspend fun patchTitle(dateKey: String, title: String?): DayRouteTitle {
                    callOrder += "title"
                    throw IllegalStateException("boom")
                }
            },
            memoRepository = object : DayRouteMemoRepository {
                override suspend fun patchMemo(dateKey: String, memo: String?): DayRouteMemo {
                    callOrder += "memo"
                    return DayRouteMemo(memo = memo)
                }
            }
        )
        viewModel.syncSelectedDay(dateKey = "2026-04-02", title = "Old", memo = "Old memo")
        viewModel.updateTitle("Updated title")
        viewModel.updateMemo("Updated memo")

        viewModel.submitDayNote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("title"), callOrder)
        assertEquals("네트워크 문제로 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.", state.errorMessage)
        assertNull(state.successMessage)
        assertTrue(state.isDirty)
        assertEquals(1L, state.feedbackEventId)
    }

    @Test
    fun `submitDayNote keeps successful title when memo save fails and emits partial patch`() = runTest {
        val viewModel = createViewModel(
            titleRepository = object : DayRouteTitleRepository {
                override suspend fun patchTitle(dateKey: String, title: String?): DayRouteTitle {
                    return DayRouteTitle(title = title)
                }
            },
            memoRepository = object : DayRouteMemoRepository {
                override suspend fun patchMemo(dateKey: String, memo: String?): DayRouteMemo {
                    throw IllegalStateException("memo failure")
                }
            }
        )
        viewModel.syncSelectedDay(dateKey = "2026-04-02", title = "Old", memo = "Old memo")
        viewModel.updateTitle("New title")
        viewModel.updateMemo("New memo")
        val patchDeferred = async { viewModel.snapshotPatch.first() }

        viewModel.submitDayNote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val patch = patchDeferred.await()
        assertEquals("New title", state.originalTitle)
        assertEquals("New title", state.title)
        assertEquals("Old memo", state.originalMemo)
        assertEquals("New memo", state.memo)
        assertEquals("네트워크 문제로 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.", state.errorMessage)
        assertNull(state.successMessage)
        assertTrue(state.isDirty)
        assertTrue(patch.shouldUpdateTitle)
        assertFalse(patch.shouldUpdateMemo)
        assertEquals("New title", patch.title)
        assertEquals("Old memo", patch.memo)
    }

    @Test
    fun `updateTitle and updateMemo enforce max lengths`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateTitle("a".repeat(DayNoteViewModel.MAX_TITLE_LENGTH + 10))
        viewModel.updateMemo("b".repeat(DayNoteViewModel.MAX_MEMO_LENGTH + 10))

        val state = viewModel.uiState.value
        assertEquals(DayNoteViewModel.MAX_TITLE_LENGTH, state.title.length)
        assertEquals(DayNoteViewModel.MAX_MEMO_LENGTH, state.memo.length)
    }

    private fun createViewModel(
        titleRepository: DayRouteTitleRepository = object : DayRouteTitleRepository {
            override suspend fun patchTitle(dateKey: String, title: String?): DayRouteTitle {
                return DayRouteTitle(title = title)
            }
        },
        memoRepository: DayRouteMemoRepository = object : DayRouteMemoRepository {
            override suspend fun patchMemo(dateKey: String, memo: String?): DayRouteMemo {
                return DayRouteMemo(memo = memo)
            }
        }
    ): DayNoteViewModel {
        return DayNoteViewModel(
            patchDayRouteTitleUseCase = PatchDayRouteTitleUseCase(titleRepository),
            patchDayRouteMemoUseCase = PatchDayRouteMemoUseCase(memoRepository),
            initialDateKey = "2026-04-01"
        )
    }
}
