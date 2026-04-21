package com.example.passedpath.feature.place.data.remote.mapper

import com.example.passedpath.feature.place.data.remote.dto.BookmarkPlaceUpdateRequestDto
import com.example.passedpath.feature.place.data.remote.dto.BookmarkPlaceUpdateResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceAddRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceAddResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceListItemDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceListResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceUpdateRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceUpdateResponseDto
import com.example.passedpath.feature.place.domain.model.BookmarkPlace
import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.PlaceSourceType
import com.example.passedpath.feature.place.domain.model.RegisteredPlace
import com.example.passedpath.feature.place.domain.model.UpdatedPlace
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.domain.model.VisitedPlaceList

internal fun PlaceRegistration.toRequestDto(): PlaceAddRequestDto {
    return PlaceAddRequestDto(
        roadAddress = roadAddress,
        placeName = placeName,
        latitude = latitude,
        longitude = longitude
    )
}

internal fun PlaceRegistration.toUpdateRequestDto(): PlaceUpdateRequestDto {
    return PlaceUpdateRequestDto(
        roadAddress = roadAddress,
        placeName = placeName,
        latitude = latitude,
        longitude = longitude
    )
}

internal fun BookmarkPlace.toUpdateBookmarkPlaceRequestDto(): BookmarkPlaceUpdateRequestDto {
    return BookmarkPlaceUpdateRequestDto(
        type = type.name,
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude
    )
}

internal fun PlaceAddResponseDto.toRegisteredPlace(): RegisteredPlace {
    return RegisteredPlace(
        placeId = placeId,
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude,
        orderIndex = orderIndex
    )
}

internal fun PlaceListResponseDto.toVisitedPlaceList(): VisitedPlaceList {
    val mappedPlaces = places.orEmpty()
        .mapNotNull(PlaceListItemDto::toVisitedPlaceOrNull)
        .sortedBy(VisitedPlace::orderIndex)

    return VisitedPlaceList(
        placeCount = placeCount ?: mappedPlaces.size,
        places = mappedPlaces
    )
}

internal fun PlaceUpdateResponseDto.toUpdatedPlace(): UpdatedPlace {
    return UpdatedPlace(
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude
    )
}

internal fun BookmarkPlaceUpdateResponseDto.toBookmarkPlace(): BookmarkPlace {
    return BookmarkPlace(
        type = BookmarkPlaceType.valueOf(type),
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude
    )
}

private fun PlaceListItemDto.toVisitedPlaceOrNull(): VisitedPlace? {
    val resolvedPlaceId = placeId ?: return null
    val resolvedType = type?.toPlaceSourceTypeOrNull() ?: return null
    val resolvedLatitude = latitude ?: return null
    val resolvedLongitude = longitude ?: return null
    val resolvedOrderIndex = orderIndex ?: return null

    return VisitedPlace(
        placeId = resolvedPlaceId,
        placeName = placeName.orEmpty(),
        type = resolvedType,
        roadAddress = roadAddress.orEmpty(),
        latitude = resolvedLatitude,
        longitude = resolvedLongitude,
        orderIndex = resolvedOrderIndex
    )
}

private fun String.toPlaceSourceTypeOrNull(): PlaceSourceType? {
    return kotlin.runCatching { PlaceSourceType.valueOf(this) }.getOrElse { null }
}
