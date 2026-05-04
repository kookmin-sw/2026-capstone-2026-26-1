package com.example.passedpath.feature.permission.presentation.policy

import com.example.passedpath.feature.permission.data.manager.LocationPermissionStatusReader
import com.example.passedpath.feature.permission.data.manager.LocationServiceStatusReader
import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationAccessStateResolverTest {

    @Test
    fun `resolves always permission when background permission is granted`() {
        val resolver = createResolver(
            foregroundGranted = false,
            backgroundGranted = true
        )

        assertEquals(LocationPermissionUiState.ALWAYS, resolver.resolvePermissionState())
    }

    @Test
    fun `resolves foreground only permission when only foreground permission is granted`() {
        val resolver = createResolver(
            foregroundGranted = true,
            backgroundGranted = false
        )

        assertEquals(LocationPermissionUiState.FOREGROUND_ONLY, resolver.resolvePermissionState())
    }

    @Test
    fun `resolves denied permission when no location permission is granted`() {
        val resolver = createResolver(
            foregroundGranted = false,
            backgroundGranted = false
        )

        assertEquals(LocationPermissionUiState.DENIED, resolver.resolvePermissionState())
    }

    @Test
    fun `returns location service state from reader`() {
        assertTrue(createResolver(locationServiceEnabled = true).isLocationServiceEnabled())
        assertFalse(createResolver(locationServiceEnabled = false).isLocationServiceEnabled())
    }

    private fun createResolver(
        foregroundGranted: Boolean = false,
        backgroundGranted: Boolean = false,
        locationServiceEnabled: Boolean = true
    ): LocationAccessStateResolver {
        return LocationAccessStateResolver(
            locationPermissionStatusReader = FakeLocationPermissionStatusReader(
                foregroundGranted = foregroundGranted,
                backgroundGranted = backgroundGranted
            ),
            locationServiceStatusReader = FakeLocationServiceStatusReader(
                isEnabled = locationServiceEnabled
            )
        )
    }

    private class FakeLocationPermissionStatusReader(
        private val foregroundGranted: Boolean,
        private val backgroundGranted: Boolean
    ) : LocationPermissionStatusReader {
        override fun isForegroundGranted(): Boolean = foregroundGranted
        override fun isBackgroundAlwaysGranted(): Boolean = backgroundGranted
    }

    private class FakeLocationServiceStatusReader(
        private val isEnabled: Boolean
    ) : LocationServiceStatusReader {
        override fun isLocationServiceEnabled(): Boolean = isEnabled
    }
}
