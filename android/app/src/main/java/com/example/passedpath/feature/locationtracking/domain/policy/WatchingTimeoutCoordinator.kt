package com.example.passedpath.feature.locationtracking.domain.policy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// watching 신호(TrackingUploadModeReader.watchingSignalEvents)를 받아 IMMEDIATE 타임아웃을
// 스케줄링/리셋하는 소비 로직만 담당한다. 신호가 IMMEDIATE로 반복 도착하면(동일 값이라도)
// 매번 타이머를 새로 시작해, 신호가 끊긴 뒤에만 timeoutMillis 후 onTimeout이 호출된다.
class WatchingTimeoutCoordinator(
    private val scope: CoroutineScope,
    private val timeoutMillis: Long = LocationUploadPolicy.WATCHING_TIMEOUT_MS,
    private val onTimeout: suspend () -> Unit
) {
    private var timeoutJob: Job? = null

    fun onSignal(mode: TrackingUploadMode) {
        timeoutJob?.cancel()
        timeoutJob = if (mode == TrackingUploadMode.IMMEDIATE) {
            scope.launch {
                delay(timeoutMillis)
                onTimeout()
            }
        } else {
            null
        }
    }

    fun cancel() {
        timeoutJob?.cancel()
        timeoutJob = null
    }
}
