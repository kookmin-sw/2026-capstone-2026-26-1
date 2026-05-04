package com.example.passedpath.feature.placebookmark.data.remote.mapper

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkCreateRequestDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkCreateResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkListResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkSummaryResponseDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateRequestDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateResponseDto
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkList
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary
import com.example.passedpath.feature.placebookmark.domain.model.RegisteredPlaceBookmark

internal fun PlaceBookmarkListResponseDto.toPlaceBookmarkList(): PlaceBookmarkList {
    val mappedPlaces = bookmarkPlaces.orEmpty()
        .mapNotNull(PlaceBookmarkSummaryResponseDto::toPlaceBookmarkSummaryOrNull)

    return PlaceBookmarkList(
        placeCount = placeCount ?: mappedPlaces.size,
        bookmarkPlaces = mappedPlaces
    )
}

internal fun PlaceBookmark.toCreateRequestDto(): PlaceBookmarkCreateRequestDto {
    return PlaceBookmarkCreateRequestDto(
        type = type.name,
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude
    )
}

internal fun PlaceBookmark.toUpdateRequestDto(): PlaceBookmarkUpdateRequestDto {
    return PlaceBookmarkUpdateRequestDto(
        type = type.name,
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude
    )
}

private fun PlaceBookmarkSummaryResponseDto.toPlaceBookmarkSummaryOrNull(): PlaceBookmarkSummary? {
    val resolvedPlaceId = placeId ?: return null
    val resolvedType = type?.toBookmarkPlaceTypeOrNull() ?: return null
    val resolvedLatitude = latitude ?: return null
    val resolvedLongitude = longitude ?: return null

    return PlaceBookmarkSummary(
        bookmarkPlaceId = resolvedPlaceId,
        type = resolvedType,
        placeName = placeName.orEmpty(),
        roadAddress = roadAddress.orEmpty(),
        latitude = resolvedLatitude,
        longitude = resolvedLongitude
    )
}

private fun String.toBookmarkPlaceTypeOrNull(): BookmarkPlaceType? {
    return kotlin.runCatching { BookmarkPlaceType.valueOf(this) }.getOrElse { null }
}

internal fun PlaceBookmarkCreateResponseDto.toRegisteredPlaceBookmark(): RegisteredPlaceBookmark {
    return RegisteredPlaceBookmark(
        bookmarkPlaceId = bookmarkPlaceId,
        type = BookmarkPlaceType.valueOf(type),
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude
    )
}

internal fun PlaceBookmarkUpdateResponseDto.toPlaceBookmark(): PlaceBookmark {
    return PlaceBookmark(
        type = BookmarkPlaceType.valueOf(type),
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude
    )
}
