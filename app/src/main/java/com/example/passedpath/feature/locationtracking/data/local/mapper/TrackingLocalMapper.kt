package com.example.passedpath.feature.locationtracking.data.local.mapper

import android.location.Location
import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.entity.GpsPointEntity
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DateKeyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

// TrackedLocation 도메인 모델을 Room 저장용 GpsPointEntity로 변환한다.
fun TrackedLocation.toGpsPointEntity(dateKey: String): GpsPointEntity {
    return GpsPointEntity(
        dateKey = dateKey,
        recordedAtEpochMillis = recordedAtEpochMillis,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        isUploaded = false
    )
}

// Room에서 읽은 GpsPointEntity를 도메인용 TrackedLocation으로 변환한다.
fun GpsPointEntity.toTrackedLocation(): TrackedLocation {
    return TrackedLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        recordedAtEpochMillis = recordedAtEpochMillis
    )
}

// 날짜별 raw GPS 포인트 목록과 요약 정보를 화면용 DailyPath로 변환한다.
fun List<GpsPointEntity>.toDailyPath(
    dateKey: String,
    existingRoute: DayRouteEntity? = null
): DailyPath {
    val trackedPoints = map(GpsPointEntity::toTrackedLocation)
    val totalDistanceMeters =
        existingRoute?.totalDistanceMeters ?: trackedPoints.calculateTotalDistanceMeters()

    return DailyPath(
        dateKey = dateKey,
        points = trackedPoints,
        totalDistanceMeters = totalDistanceMeters,
        pathPointCount = trackedPoints.size
    )
}

// 날짜별 raw GPS 포인트 목록을 일별 요약 저장용 DayRouteEntity로 변환한다.
fun List<GpsPointEntity>.toDayRouteEntity(
    dateKey: String,
    previousRoute: DayRouteEntity? = null
): DayRouteEntity {
    val trackedPoints = map(GpsPointEntity::toTrackedLocation)

    return DayRouteEntity(
        dateKey = dateKey,
        totalDistanceMeters = trackedPoints.calculateTotalDistanceMeters(),
        pathPointCount = trackedPoints.size,
        lastRecordedAtEpochMillis = lastOrNull()?.recordedAtEpochMillis,
        lastSyncedAtEpochMillis = previousRoute?.lastSyncedAtEpochMillis,
        encodedPath = previousRoute?.encodedPath
    )
}

// 새로 저장한 포인트 1개를 기준으로 일별 요약 정보를 증분(추가) 갱신한다.
// 새 gps 점이 들어올 때마다 하루 경로 전체를 다시 계산하지 말고, 마지막 점과 새 점 사이의 추가 거리만 반영해서
// day_routes 요약값을 (비교적 싸게) 갱신한다
fun DayRouteEntity?.toUpdatedDayRouteEntity(
    dateKey: String,
    newPoint: TrackedLocation,
    previousPoint: TrackedLocation?
): DayRouteEntity {
    val distanceDeltaMeters = if (previousPoint == null) {
        0.0
    } else {
        distanceBetweenMeters(previousPoint, newPoint)
    }

    return DayRouteEntity(
        dateKey = dateKey,
        totalDistanceMeters = (this?.totalDistanceMeters ?: 0.0) + distanceDeltaMeters,
        pathPointCount = (this?.pathPointCount ?: 0) + 1,
        lastRecordedAtEpochMillis = newPoint.recordedAtEpochMillis,
        lastSyncedAtEpochMillis = this?.lastSyncedAtEpochMillis,
        encodedPath = this?.encodedPath
    )
}

// epoch millis 값을 디바이스 로컬 타임존 기준 yyyy-MM-dd dateKey로 바꾼다.
fun epochMillisToDateKey(
    epochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .toLocalDate()
        .format(DateKeyFormatter)
}

// 위치 목록의 인접 점들 사이 거리를 모두 더해 총 이동거리를 계산한다.
private fun List<TrackedLocation>.calculateTotalDistanceMeters(): Double {
    if (size < 2) return 0.0

    var distanceMeters = 0.0
    for (index in 1 until size) {
        val previous = this[index - 1]
        val current = this[index]
        distanceMeters += distanceBetweenMeters(previous, current)
    }

    return distanceMeters
}

// 두 위치 좌표 사이의 실제 거리(m)를 계산한다.
fun distanceBetweenMeters(
    start: TrackedLocation,
    end: TrackedLocation
): Double {
    val results = FloatArray(1)
    Location.distanceBetween(
        start.latitude,
        start.longitude,
        end.latitude,
        end.longitude,
        results
    )
    return results.first().toDouble()
}
