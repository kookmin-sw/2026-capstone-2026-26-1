package com.example.passedpath.feature.locationtracking.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface LocationTrackingServiceStateReader {
    val isTracking: StateFlow<Boolean>
}

interface LocationTrackingServiceStateWriter {
    fun update(isTracking: Boolean)
}

class InMemoryLocationTrackingServiceStateHolder :
    LocationTrackingServiceStateReader,
    LocationTrackingServiceStateWriter {

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    override fun update(isTracking: Boolean) {
        _isTracking.value = isTracking
    }
}
