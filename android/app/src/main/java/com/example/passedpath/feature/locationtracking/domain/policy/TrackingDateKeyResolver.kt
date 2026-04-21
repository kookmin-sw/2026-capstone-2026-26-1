package com.example.passedpath.feature.locationtracking.domain.policy

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 하루 경계 시간을 기준으로 timestamp의 datekey를 결정하는 클래스
class TrackingDateKeyResolver(
    private val boundaryTimeProvider: TrackingDayBoundaryTimeProvider,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // 주어진 시각이 어떤 tracking dateKey에 속하는지 경계 시각 기준으로 계산한다.
    fun resolveDateKey(epochMillis: Long): String {
        val zonedDateTime = Instant.ofEpochMilli(epochMillis).atZone(zoneId)
        val boundaryLocalTime = boundaryTimeProvider.getBoundaryLocalTime()

        // boundary time 이전이면 전날/boundary time 이후면 다음날
        val resolvedDate = if (zonedDateTime.toLocalTime().isBefore(boundaryLocalTime)) {
            zonedDateTime.toLocalDate().minusDays(1)
        } else {
            zonedDateTime.toLocalDate()
        }

        return resolvedDate.format(dateFormatter)
    }

    // 지금 시간 기준 datekey 계산하는 파생함수
    fun resolveCurrentDateKey(nowEpochMillis: Long = System.currentTimeMillis()): String {
        return resolveDateKey(nowEpochMillis)
    }

    // 지금 시간 기준 datekey의 하루 전 datekey 반환하는 파생함수
    fun resolvePreviousDateKey(nowEpochMillis: Long = System.currentTimeMillis()): String {
        return LocalDate.parse(resolveCurrentDateKey(nowEpochMillis), dateFormatter)
            .minusDays(1)
            .format(dateFormatter)
    }

    // 다음 tracking 날짜 경계 시각까지 남은 시간을 밀리초로 계산한다.
    fun millisUntilNextBoundary(nowEpochMillis: Long = System.currentTimeMillis()): Long {
        val now = Instant.ofEpochMilli(nowEpochMillis).atZone(zoneId)
        val boundaryLocalTime = boundaryTimeProvider.getBoundaryLocalTime()

        // boundary time 이전이면 전날/boundary time 이후면 다음날
        val nextBoundary = if (now.toLocalTime().isBefore(boundaryLocalTime)) {
            now.toLocalDate().atTime(boundaryLocalTime).atZone(zoneId)
        } else {
            now.toLocalDate().plusDays(1).atTime(boundaryLocalTime).atZone(zoneId)
        }

        return Duration.between(now, nextBoundary).toMillis().coerceAtLeast(0L)
    }

    // 경계 직전 flush를 위해 다음 날짜 경계까지의 남은 시간에서 리드 타임을 뺀 값을 계산한다.
    fun millisUntilPreBoundaryFlush(
        leadTimeMillis: Long,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): Long {
        return (millisUntilNextBoundary(nowEpochMillis) - leadTimeMillis).coerceAtLeast(0L)
    }
}
