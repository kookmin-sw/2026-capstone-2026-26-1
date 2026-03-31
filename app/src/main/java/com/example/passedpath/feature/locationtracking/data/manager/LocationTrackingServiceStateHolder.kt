package com.example.passedpath.feature.locationtracking.data.manager

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface LocationTrackingServiceStateReader {
    val isTracking: StateFlow<Boolean>
    fun isTrackingEnabledByUser(): Boolean
}

interface LocationTrackingServiceStateWriter {
    fun update(isTracking: Boolean)
    fun setTrackingEnabledByUser(isEnabled: Boolean)
}

class PersistentLocationTrackingServiceStateHolder(
    context: Context
) : LocationTrackingServiceStateReader, LocationTrackingServiceStateWriter {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _isTracking = MutableStateFlow(false)

    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    override fun isTrackingEnabledByUser(): Boolean {
        return preferences.getBoolean(KEY_USER_ENABLED, true)
    }

    override fun update(isTracking: Boolean) {
        _isTracking.value = isTracking
    }

    override fun setTrackingEnabledByUser(isEnabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_USER_ENABLED, isEnabled)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "location_tracking_state"
        const val KEY_USER_ENABLED = "user_enabled"
    }
}

class InMemoryLocationTrackingServiceStateHolder(
    private var isTrackingEnabledByUser: Boolean = true
) : LocationTrackingServiceStateReader, LocationTrackingServiceStateWriter {

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    override fun isTrackingEnabledByUser(): Boolean = isTrackingEnabledByUser

    override fun update(isTracking: Boolean) {
        _isTracking.value = isTracking
    }

    override fun setTrackingEnabledByUser(isEnabled: Boolean) {
        isTrackingEnabledByUser = isEnabled
    }
}
