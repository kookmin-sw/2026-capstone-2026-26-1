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
    fun `tab selection policy opens hidden sheet to middle`() {
        val decision = resolveBottomSheetTabSelection(
            currentSheetValue = MainBottomSheetValue.HIDDEN,
            currentTab = MainBottomSheetTab.PLACE,
            selectedTab = MainBottomSheetTab.DAYNOTE,
            selectedPlaceId = 9L
        )

        assertEquals(MainBottomSheetValue.MIDDLE, decision.requestedSheetValue)
        assertNull(decision.selectedPlaceId)
        assertFalse(decision.shouldRefreshPlaces)
    }

    @Test
    fun `tab selection policy keeps visible sheet height unchanged`() {
        val decision = resolveBottomSheetTabSelection(
            currentSheetValue = MainBottomSheetValue.EXPANDED,
            currentTab = MainBottomSheetTab.DAYNOTE,
            selectedTab = MainBottomSheetTab.PLACE,
            selectedPlaceId = 9L
        )

        assertNull(decision.requestedSheetValue)
        assertEquals(9L, decision.selectedPlaceId)
        assertTrue(decision.shouldRefreshPlaces)
    }

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
    fun `place tab reselection opens hidden sheet to middle and requests refresh`() {
        val initialState = MainScreenLocalUiState(
            selectedBottomSheetTab = MainBottomSheetTab.PLACE,
            bottomSheetValue = MainBottomSheetValue.HIDDEN,
            requestedSheetValue = MainBottomSheetValue.HIDDEN
        )

        val result = reduceForBottomSheetTabSelection(
            state = initialState,
            selectedTab = MainBottomSheetTab.PLACE
        )

        assertEquals(MainBottomSheetTab.PLACE, result.state.selectedBottomSheetTab)
        assertEquals(MainBottomSheetValue.MIDDLE, result.state.requestedSheetValue)
        assertTrue(result.shouldRefreshPlaces)
    }

    @Test
    fun `daynote tab selection keeps expanded sheet height and clears selected place`() {
        val initialState = MainScreenLocalUiState(
            selectedBottomSheetTab = MainBottomSheetTab.PLACE,
            bottomSheetValue = MainBottomSheetValue.EXPANDED,
            selectedPlaceId = 9L
        )

        val result = reduceForBottomSheetTabSelection(
            state = initialState,
            selectedTab = MainBottomSheetTab.DAYNOTE
        )

        assertEquals(MainBottomSheetTab.DAYNOTE, result.state.selectedBottomSheetTab)
        assertNull(result.state.requestedSheetValue)
        assertNull(result.state.selectedPlaceId)
        assertFalse(result.shouldRefreshPlaces)
    }

    @Test
    fun `sheet value change keeps requested command until command completion callback`() {
        val initialState = MainScreenLocalUiState(
            bottomSheetValue = MainBottomSheetValue.HIDDEN,
            requestedSheetValue = MainBottomSheetValue.MIDDLE
        )

        val result = reduceForSheetValueChange(
            state = initialState,
            bottomSheetValue = MainBottomSheetValue.MIDDLE
        )

        assertEquals(MainBottomSheetValue.MIDDLE, result.state.bottomSheetValue)
        assertEquals(MainBottomSheetValue.MIDDLE, result.state.requestedSheetValue)
    }

    @Test
    fun `sheet command consumed clears matching requested value`() {
        val initialState = MainScreenLocalUiState(
            bottomSheetValue = MainBottomSheetValue.HIDDEN,
            requestedSheetValue = MainBottomSheetValue.MIDDLE
        )

        val result = reduceForSheetCommandConsumed(
            state = initialState,
            consumedValue = MainBottomSheetValue.MIDDLE
        )

        assertNull(result.state.requestedSheetValue)
    }

    @Test
    fun `selected place handled clears one time marker selection only`() {
        val initialState = MainScreenLocalUiState(
            selectedBottomSheetTab = MainBottomSheetTab.PLACE,
            bottomSheetValue = MainBottomSheetValue.MIDDLE,
            selectedPlaceId = 9L
        )

        val result = reduceForSelectedPlaceHandled(initialState)

        assertEquals(MainBottomSheetTab.PLACE, result.state.selectedBottomSheetTab)
        assertEquals(MainBottomSheetValue.MIDDLE, result.state.bottomSheetValue)
        assertNull(result.state.selectedPlaceId)
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

    @Test
    fun `current location button shows only when sheet is hidden`() {
        assertTrue(shouldShowCurrentLocationButton(MainBottomSheetValue.HIDDEN))
        assertFalse(shouldShowCurrentLocationButton(MainBottomSheetValue.MIDDLE))
        assertFalse(shouldShowCurrentLocationButton(MainBottomSheetValue.EXPANDED))
    }
}
