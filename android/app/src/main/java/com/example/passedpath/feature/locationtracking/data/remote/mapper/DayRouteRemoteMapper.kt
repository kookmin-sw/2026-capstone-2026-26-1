package com.example.passedpath.feature.locationtracking.data.remote.mapper

import com.example.passedpath.feature.locationtracking.data.remote.dto.DayRouteDetailResponseDto
import com.example.passedpath.feature.locationtracking.data.remote.dto.PlaceItemDto
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint

// 서버 응답 DTO를 앱 내부에서 쓰는 DayRouteDetail 도메인 모델로 정규화한다.
fun DayRouteDetailResponseDto.toDayRouteDetail(requestedDateKey: String): DayRouteDetail {
    val normalizedEncodedPath = encodedPath.orEmpty()
    // 서버의 encoded polyline 문자열을 지도 렌더링용 좌표 목록으로 디코드한다.
    val decodedPoints = decodeEncodedPath(normalizedEncodedPath)

    return DayRouteDetail(
        dateKey = date ?: requestedDateKey,
        totalDistanceKm = totalDistance ?: 0.0,
        title = title.orEmpty(),
        memo = memo.orEmpty(),
        isBookmarked = isBookmarked ?: false,
        encodedPath = normalizedEncodedPath,
        pathPointCount = pathPointCount ?: decodedPoints.size,
        polylinePoints = decodedPoints,
        places = places.orEmpty()
            .mapNotNull(PlaceItemDto::toDayRoutePlaceOrNull)
            .sortedBy(DayRoutePlace::orderIndex)
    )
}

// place 항목은 좌표와 순서가 있어야 지도 마커로 그릴 수 있으므로, 핵심 값이 없으면 버린다.
private fun PlaceItemDto.toDayRoutePlaceOrNull(): DayRoutePlace? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    val index = orderIndex ?: return null

    return DayRoutePlace(
        placeId = placeId ?: 0L,
        placeName = placeName.orEmpty(),
        roadAddress = roadAddress.orEmpty(),
        latitude = lat,
        longitude = lng,
        orderIndex = index
    )
}

// Google encoded polyline 형식을 순차적으로 읽어 RoutePoint 목록으로 복원한다.
private fun decodeEncodedPath(encodedPath: String): List<RoutePoint> {
    if (encodedPath.isBlank()) return emptyList()

    val points = mutableListOf<RoutePoint>()
    var index = 0
    var latitude = 0
    var longitude = 0

    while (index < encodedPath.length) {
        val latitudeResult = decodeValue(encodedPath, index)
        latitude += latitudeResult.delta
        index = latitudeResult.nextIndex

        if (index >= encodedPath.length) break

        val longitudeResult = decodeValue(encodedPath, index)
        longitude += longitudeResult.delta
        index = longitudeResult.nextIndex

        points += RoutePoint(
            latitude = latitude / 1E5,
            longitude = longitude / 1E5
        )
    }

    return points
}

// polyline 문자열에서 위도 또는 경도 한 축의 delta 값을 하나씩 디코드한다.
private fun decodeValue(encodedPath: String, startIndex: Int): DecodedValue {
    var result = 0
    var shift = 0
    var index = startIndex

    while (index < encodedPath.length) {
        val value = encodedPath[index].code - 63
        result = result or ((value and 0x1F) shl shift)
        index += 1
        shift += 5
        if (value < 0x20) break
    }

    val delta = if (result and 1 != 0) {
        (result shr 1).inv()
    } else {
        result shr 1
    }

    return DecodedValue(delta = delta, nextIndex = index)
}

private data class DecodedValue(
    val delta: Int,
    val nextIndex: Int
)
