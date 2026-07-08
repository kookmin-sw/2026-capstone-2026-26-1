package com.example.passedpath.feature.locationtracking.data.manager

import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingUploadMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TrackingUploadModeReader {
    val uploadMode: StateFlow<TrackingUploadMode>
}

interface TrackingUploadModeWriter {
    fun update(mode: TrackingUploadMode)
}

class InMemoryTrackingUploadModeStateHolder :
    TrackingUploadModeReader,
    TrackingUploadModeWriter {

    private val _uploadMode = MutableStateFlow(TrackingUploadMode.NORMAL)
    override val uploadMode: StateFlow<TrackingUploadMode> = _uploadMode.asStateFlow()

    override fun update(mode: TrackingUploadMode) {
        AppDebugLogger.debug(
            DebugLogTag.TRACKING,
            "upload mode update mode=$mode"
        )
        _uploadMode.value = mode
    }
}
