package com.example.passedpath.feature.daynote.presentation.viewmodel

import com.example.passedpath.feature.daynote.domain.model.DayRouteMemo
import com.example.passedpath.feature.daynote.domain.model.DayRouteTitle
import com.example.passedpath.feature.daynote.domain.repository.DayRouteMemoRepository
import com.example.passedpath.feature.daynote.domain.repository.DayRouteTitleRepository
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteMemoUseCase
import com.example.passedpath.feature.daynote.domain.usecase.PatchDayRouteTitleUseCase
import com.example.passedpath.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    }

    @Test
    fun `submitTitle updates originalTitle with saved result`() = runTest {
        val viewModel = createViewModel(
            titleRepository = object : DayRouteTitleRepository {
                override suspend fun patchTitle(dateKey: String, title: String?): DayRouteTitle {
                    return DayRouteTitle(title = "Saved title")
                }
            }
        )
        viewModel.syncSelectedDay(dateKey = "2026-04-02", title = "", memo = "")
        viewModel.updateTitle("Draft")

        viewModel.submitTitle()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Saved title", state.originalTitle)
        assertEquals("Saved title", state.title)
    }

    @Test
    fun `submitMemo updates originalMemo with saved result`() = runTest {
        val viewModel = createViewModel(
            memoRepository = object : DayRouteMemoRepository {
                override suspend fun patchMemo(dateKey: String, memo: String?): DayRouteMemo {
                    return DayRouteMemo(memo = "Saved memo")
                }
            }
        )
        viewModel.syncSelectedDay(dateKey = "2026-04-02", title = "", memo = "")
        viewModel.updateMemo("Draft")

        viewModel.submitMemo()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Saved memo", state.originalMemo)
        assertEquals("Saved memo", state.memo)
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
