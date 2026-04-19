package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.main.presentation.screen.MainBottomSheetTab
import com.example.passedpath.feature.main.presentation.screen.MainBottomSheetValue
import com.example.passedpath.feature.main.presentation.screen.MainScreenLocalUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenInteractionPolicyTest {

    @Test
    fun `marker click from non place tab opens place tab and requests refresh`() {
        val initialState = MainScreenLocalUiState(
            selectedBottomSheetTab = MainBottomSheetTab.DAYNOTE
        )

        val result = reduceForPlaceMarkerClick(
            state = initialState,
            placeId = 7L
        )

        assertEquals(MainBottomSheetTab.PLACE, result.state.selectedBottomSheetTab)
        assertEquals(MainBottomSheetValue.MIDDLE, result.state.requestedSheetValue)
        assertEquals(7L, result.state.selectedPlaceId)
        assertTrue(result.shouldRefreshPlaces)
    }

    @Test
    fun `marker click from place tab keeps refresh off`() {
        val initialState = MainScreenLocalUiState(
            selectedBottomSheetTab = MainBottomSheetTab.PLACE
        )

        val result = reduceForPlaceMarkerClick(
            state = initialState,
            placeId = 3L
        )

        assertFalse(result.shouldRefreshPlaces)
    }

    @Test
    fun `daynote tab selection clears selected place without refresh`() {
        val initialState = MainScreenLocalUiState(
            selectedBottomSheetTab = MainBottomSheetTab.PLACE,
            selectedPlaceId = 9L
        )

        val result = reduceForBottomSheetTabSelection(
            state = initialState,
            selectedTab = MainBottomSheetTab.DAYNOTE
        )

        assertEquals(MainBottomSheetTab.DAYNOTE, result.state.selectedBottomSheetTab)
        assertEquals(MainBottomSheetValue.MIDDLE, result.state.requestedSheetValue)
        assertNull(result.state.selectedPlaceId)
        assertFalse(result.shouldRefreshPlaces)
    }

    @Test
    fun `sheet hide request sets hidden sheet and clears selected place`() {
        val initialState = MainScreenLocalUiState(
            bottomSheetValue = MainBottomSheetValue.EXPANDED,
            requestedSheetValue = MainBottomSheetValue.EXPANDED,
            selectedPlaceId = 4L
        )

        val result = reduceForSheetHideRequest(initialState)

        assertEquals(MainBottomSheetValue.HIDDEN, result.state.requestedSheetValue)
        assertNull(result.state.selectedPlaceId)
    }

    @Test
    fun `hidden sheet clears selected place and requested value`() {
        val initialState = MainScreenLocalUiState(
            bottomSheetValue = MainBottomSheetValue.MIDDLE,
            requestedSheetValue = MainBottomSheetValue.MIDDLE,
            selectedPlaceId = 5L
        )

        val result = reduceForSheetValueChange(
            state = initialState,
            bottomSheetValue = MainBottomSheetValue.HIDDEN
        )

        assertEquals(MainBottomSheetValue.HIDDEN, result.state.bottomSheetValue)
        assertNull(result.state.requestedSheetValue)
        assertNull(result.state.selectedPlaceId)
    }
}
