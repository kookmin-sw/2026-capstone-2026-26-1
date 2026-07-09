package com.example.passedpath.feature.locationtracking.domain.policy

import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private const val TIMEOUT_MILLIS = 10 * 60_000L

class WatchingTimeoutCoordinatorTest {

    @Test
    fun `IMMEDIATE 신호 후 타임아웃 시간이 지나면 onTimeout이 호출된다`() = runTest {
        var timeoutCount = 0
        val coordinator = WatchingTimeoutCoordinator(
            scope = this,
            timeoutMillis = TIMEOUT_MILLIS,
            onTimeout = { timeoutCount++ }
        )

        coordinator.onSignal(TrackingUploadMode.IMMEDIATE)
        advanceTimeBy(TIMEOUT_MILLIS + 1)
        runCurrent()

        assertEquals(1, timeoutCount)
    }

    @Test
    fun `타임아웃 전에 같은 IMMEDIATE 신호가 반복 도착하면 타이머가 매번 리셋되어 onTimeout이 호출되지 않는다`() = runTest {
        var timeoutCount = 0
        val coordinator = WatchingTimeoutCoordinator(
            scope = this,
            timeoutMillis = TIMEOUT_MILLIS,
            onTimeout = { timeoutCount++ }
        )

        // 5분마다 재발송되는 백엔드 시나리오를 흉내낸다 (타임아웃 10분보다 짧은 주기로 반복 신호).
        coordinator.onSignal(TrackingUploadMode.IMMEDIATE)
        advanceTimeBy(5 * 60_000L)
        runCurrent()
        coordinator.onSignal(TrackingUploadMode.IMMEDIATE)
        advanceTimeBy(5 * 60_000L)
        runCurrent()
        coordinator.onSignal(TrackingUploadMode.IMMEDIATE)
        advanceTimeBy(5 * 60_000L)
        runCurrent()

        assertEquals(0, timeoutCount)
    }

    @Test
    fun `NORMAL 신호가 오면 대기 중이던 타이머가 취소된다`() = runTest {
        var timeoutCount = 0
        val coordinator = WatchingTimeoutCoordinator(
            scope = this,
            timeoutMillis = TIMEOUT_MILLIS,
            onTimeout = { timeoutCount++ }
        )

        coordinator.onSignal(TrackingUploadMode.IMMEDIATE)
        advanceTimeBy(TIMEOUT_MILLIS / 2)
        runCurrent()
        coordinator.onSignal(TrackingUploadMode.NORMAL)
        advanceTimeBy(TIMEOUT_MILLIS)
        runCurrent()

        assertEquals(0, timeoutCount)
    }

    @Test
    fun `cancel을 호출하면 대기 중이던 타이머가 취소된다`() = runTest {
        var timeoutCount = 0
        val coordinator = WatchingTimeoutCoordinator(
            scope = this,
            timeoutMillis = TIMEOUT_MILLIS,
            onTimeout = { timeoutCount++ }
        )

        coordinator.onSignal(TrackingUploadMode.IMMEDIATE)
        advanceTimeBy(TIMEOUT_MILLIS / 2)
        runCurrent()
        coordinator.cancel()
        advanceTimeBy(TIMEOUT_MILLIS)
        runCurrent()

        assertEquals(0, timeoutCount)
    }
}
