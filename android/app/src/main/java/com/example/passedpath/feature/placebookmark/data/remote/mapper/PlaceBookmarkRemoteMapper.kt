package com.example.passedpath.feature.placebookmark.data.remote.mapper

import com.example.passedpath.feature.place.domain.model.BookmarkPlaceType
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateRequestDto
import com.example.passedpath.feature.placebookmark.data.remote.dto.PlaceBookmarkUpdateResponseDto
import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmark

internal fun PlaceBookmark.toUpdateRequestDto(): PlaceBookmarkUpdateRequestDto {
    return PlaceBookmarkUpdateRequestDto(
        type = type.name,
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
