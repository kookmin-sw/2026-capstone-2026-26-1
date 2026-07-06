package com.example.passedpath.feature.locationtracking.data.local.mapper

import android.location.Location
import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.entity.GpsPointEntity
import com.example.passedpath.feature.locationtracking.data.local.model.GpsPointRouteProjection
import com.example.passedpath.feature.locationtracking.data.remote.dto.GpsPointRequestDto
import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.LocalDayRouteSnapshot
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.policy.MapPolylineSimplificationPolicy
import com.google.gson.Gson
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DateKeyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val RoutePolylineCacheGson = Gson()

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

fun GpsPointRouteProjection.toTrackedLocation(): TrackedLocation {
    return TrackedLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        recordedAtEpochMillis = recordedAtEpochMillis
    )
}

// TrackedLocation 도메인 모델을 업로드용 GpsPointRequestDto로 변환한다.
fun TrackedLocation.toGpsPointRequestDto(): GpsPointRequestDto {
    return GpsPointRequestDto(
        recordedAt = Instant.ofEpochMilli(recordedAtEpochMillis).toString(),
        latitude = latitude,
        longitude = longitude
    )
}

// 날짜별 route 표시용 GPS projection과 요약 정보를 화면 관찰용 snapshot으로 변환한다.
fun List<GpsPointRouteProjection>.toLocalDayRouteSnapshot(
    dateKey: String,
    existingRoute: DayRouteEntity? = null
): LocalDayRouteSnapshot {
    val trackedPoints = map(GpsPointRouteProjection::toTrackedLocation)
    val mapPolylinePoints = toRouteProjectionMapPolylineCachePoints()

    return LocalDayRouteSnapshot(
        dateKey = dateKey,
        points = mapPolylinePoints,
        totalDistanceMeters = existingRoute?.totalDistanceMeters
            ?: trackedPoints.calculateTotalDistanceMeters(),
        pathPointCount = existingRoute?.pathPointCount ?: trackedPoints.size
    )
}

fun DayRouteEntity.toLocalDayRouteSnapshotFromCache(): LocalDayRouteSnapshot? {
    val cachePoints = decodeFreshMapPolylineCacheOrNull() ?: return null
    return LocalDayRouteSnapshot(
        dateKey = dateKey,
        points = cachePoints,
        totalDistanceMeters = totalDistanceMeters,
        pathPointCount = pathPointCount
    )
}

fun DayRouteEntity.withRebuiltMapPolylineCache(
    points: List<GpsPointRouteProjection>
): DayRouteEntity {
    val cachePoints = points.toRouteProjectionMapPolylineCachePoints()
    return copy(
        mapPolylineCacheJson = cachePoints.toMapPolylineCacheJson(),
        mapPolylineCacheSourcePointCount = points.size
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
    val cachePoints = toGpsEntityMapPolylineCachePoints()

    return DayRouteEntity(
        dateKey = dateKey,
        totalDistanceMeters = trackedPoints.calculateTotalDistanceMeters(),
        pathPointCount = trackedPoints.size,
        lastRecordedAtEpochMillis = lastOrNull()?.recordedAtEpochMillis,
        lastSyncedAtEpochMillis = previousRoute?.lastSyncedAtEpochMillis,
        mapPolylineCacheJson = cachePoints.toMapPolylineCacheJson(),
        mapPolylineCacheSourcePointCount = trackedPoints.size
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
    val nextPathPointCount = (this?.pathPointCount ?: 0) + 1
    val cachePoints = when {
        this == null -> listOf(newPoint).toSimplifiedMapPolylineCachePoints()
        else -> decodeFreshMapPolylineCacheOrNull()
            ?.let { cachePoints -> (cachePoints + newPoint).toSimplifiedMapPolylineCachePoints() }
    }

    return DayRouteEntity(
        dateKey = dateKey,
        totalDistanceMeters = (this?.totalDistanceMeters ?: 0.0) + distanceDeltaMeters,
        pathPointCount = nextPathPointCount,
        lastRecordedAtEpochMillis = newPoint.recordedAtEpochMillis,
        lastSyncedAtEpochMillis = this?.lastSyncedAtEpochMillis,
        mapPolylineCacheJson = cachePoints?.toMapPolylineCacheJson()
            ?: this?.mapPolylineCacheJson
            ?: "[]",
        mapPolylineCacheSourcePointCount = if (cachePoints == null) {
            this?.mapPolylineCacheSourcePointCount ?: 0
        } else {
            nextPathPointCount
        }
    )
}

fun DayRouteEntity.decodeFreshMapPolylineCacheOrNull(): List<TrackedLocation>? {
    if (mapPolylineCacheSourcePointCount != pathPointCount) return null

    val cachePoints = mapPolylineCacheJson.decodeMapPolylineCacheOrNull() ?: return null
    if (cachePoints.size > MapPolylineSimplificationPolicy.MAX_MAP_POLYLINE_POINTS) return null

    return when {
        pathPointCount == 0 && cachePoints.isEmpty() -> cachePoints
        pathPointCount > 0 && cachePoints.isNotEmpty() -> cachePoints
        else -> null
    }
}

fun String.decodeMapPolylineCacheOrNull(): List<TrackedLocation>? {
    return runCatching {
        RoutePolylineCacheGson.fromJson(this, Array<MapPolylineCachePointJson>::class.java)
            ?.map { point ->
                TrackedLocation(
                    latitude = point.latitude,
                    longitude = point.longitude,
                    accuracyMeters = point.accuracyMeters,
                    recordedAtEpochMillis = point.recordedAtEpochMillis
                )
            }
    }.getOrNull()
}

fun List<TrackedLocation>.toMapPolylineCacheJson(): String {
    return RoutePolylineCacheGson.toJson(
        map { point ->
            MapPolylineCachePointJson(
                recordedAtEpochMillis = point.recordedAtEpochMillis,
                latitude = point.latitude,
                longitude = point.longitude,
                accuracyMeters = point.accuracyMeters
            )
        }
    )
}

private fun List<GpsPointRouteProjection>.toRouteProjectionMapPolylineCachePoints(): List<TrackedLocation> {
    val indexes = MapPolylineSimplificationPolicy.simplifyIndexes(
        size = size,
        latitudeAt = { index -> this[index].latitude },
        longitudeAt = { index -> this[index].longitude }
    )
    return indexes.map { index -> this[index].toTrackedLocation() }
}

private fun List<GpsPointEntity>.toGpsEntityMapPolylineCachePoints(): List<TrackedLocation> {
    val indexes = MapPolylineSimplificationPolicy.simplifyIndexes(
        size = size,
        latitudeAt = { index -> this[index].latitude },
        longitudeAt = { index -> this[index].longitude }
    )
    return indexes.map { index -> this[index].toTrackedLocation() }
}

private fun List<TrackedLocation>.toSimplifiedMapPolylineCachePoints(): List<TrackedLocation> {
    val indexes = MapPolylineSimplificationPolicy.simplifyIndexes(
        size = size,
        latitudeAt = { index -> this[index].latitude },
        longitudeAt = { index -> this[index].longitude }
    )
    return indexes.map { index -> this[index] }
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

// 미터 단위 거리를 서버 업로드용 킬로미터 값으로 변환한다.
fun Double.metersToKilometers(): Double {
    return this / 1000.0
}

private data class MapPolylineCachePointJson(
    val recordedAtEpochMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?
)
