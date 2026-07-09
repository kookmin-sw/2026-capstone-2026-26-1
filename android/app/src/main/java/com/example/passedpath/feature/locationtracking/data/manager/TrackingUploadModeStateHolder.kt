package com.example.passedpath.feature.locationtracking.data.manager

import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingUploadMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TrackingUploadModeReader {
    val uploadMode: StateFlow<TrackingUploadMode>

    // uploadMode(StateFlow)는 동일 값 재설정 시 conflate되어 emit되지 않으므로,
    // 백엔드의 watching=true 재발송처럼 "값은 같아도 신호는 다시 온" 이벤트를 놓치지 않기
    // 위한 별도 채널. 타임아웃 리셋처럼 매 수신마다 반응해야 하는 로직은 이 쪽을 구독한다.
    val watchingSignalEvents: SharedFlow<TrackingUploadMode>
}

interface TrackingUploadModeWriter {
    fun update(mode: TrackingUploadMode)
}

class InMemoryTrackingUploadModeStateHolder :
    TrackingUploadModeReader,
    TrackingUploadModeWriter {

    private val _uploadMode = MutableStateFlow(TrackingUploadMode.NORMAL)
    override val uploadMode: StateFlow<TrackingUploadMode> = _uploadMode.asStateFlow()

    private val _watchingSignalEvents = MutableSharedFlow<TrackingUploadMode>(
        replay = 0,
        extraBufferCapacity = 1
    )
    override val watchingSignalEvents: SharedFlow<TrackingUploadMode> = _watchingSignalEvents

    override fun update(mode: TrackingUploadMode) {
        AppDebugLogger.debug(
            DebugLogTag.TRACKING,
            "upload mode update mode=$mode"
        )
        _uploadMode.value = mode
        _watchingSignalEvents.tryEmit(mode)
    }
}
