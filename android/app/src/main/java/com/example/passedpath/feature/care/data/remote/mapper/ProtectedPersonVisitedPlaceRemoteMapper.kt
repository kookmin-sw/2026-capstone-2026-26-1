package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CarePlaceItemDto
import com.example.passedpath.feature.care.data.remote.dto.CarePlaceListResponseDto
import com.example.passedpath.feature.care.domain.model.ProtectedPersonBookmarkPlaceType
import com.example.passedpath.feature.care.domain.model.ProtectedPersonPlaceSourceType
import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlace
import com.example.passedpath.feature.care.domain.model.ProtectedPersonVisitedPlaceList

internal fun CarePlaceListResponseDto.toProtectedPersonVisitedPlaceList(): ProtectedPersonVisitedPlaceList {
    val mappedPlaces = places.orEmpty()
        .mapNotNull(CarePlaceItemDto::toProtectedPersonVisitedPlaceOrNull)
        .sortedBy(ProtectedPersonVisitedPlace::orderIndex)

    return ProtectedPersonVisitedPlaceList(
        placeCount = placeCount ?: mappedPlaces.size,
        places = mappedPlaces
    )
}

private fun CarePlaceItemDto.toProtectedPersonVisitedPlaceOrNull(): ProtectedPersonVisitedPlace? {
    val resolvedPlaceId = placeId ?: return null
    val resolvedSource = source?.toProtectedPersonPlaceSourceTypeOrNull() ?: return null
    val resolvedLatitude = latitude ?: return null
    val resolvedLongitude = longitude ?: return null
    val resolvedOrderIndex = orderIndex ?: return null

    return ProtectedPersonVisitedPlace(
        placeId = resolvedPlaceId,
        placeName = placeName.orEmpty(),
        source = resolvedSource,
        bookmarkType = type?.toProtectedPersonBookmarkPlaceTypeOrNull(),
        roadAddress = roadAddress.orEmpty(),
        latitude = resolvedLatitude,
        longitude = resolvedLongitude,
        orderIndex = resolvedOrderIndex,
        startTime = startTime,
        endTime = endTime
    )
}

private fun String.toProtectedPersonPlaceSourceTypeOrNull(): ProtectedPersonPlaceSourceType? {
    return runCatching { ProtectedPersonPlaceSourceType.valueOf(trim()) }.getOrNull()
}

private fun String.toProtectedPersonBookmarkPlaceTypeOrNull(): ProtectedPersonBookmarkPlaceType? {
    return runCatching { ProtectedPersonBookmarkPlaceType.valueOf(trim()) }.getOrNull()
}
