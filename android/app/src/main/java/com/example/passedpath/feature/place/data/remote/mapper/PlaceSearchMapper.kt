package com.example.passedpath.feature.place.data.remote.mapper

import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchItemDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchResponseDto
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult

fun PlaceSearchResponseDto.toPlaceSearchResults(): List<PlaceSearchResult> {
    return places.mapNotNull(PlaceSearchItemDto::toPlaceSearchResult)
}

private fun PlaceSearchItemDto.toPlaceSearchResult(): PlaceSearchResult? {
    val normalizedName = placeName?.trim().orEmpty()
    val normalizedCategory = category?.trim().orEmpty()
    val normalizedRoadAddress = roadAddress?.trim().orEmpty()
    val normalizedLatitude = latitude ?: return null
    val normalizedLongitude = longitude ?: return null

    if (normalizedName.isBlank()) return null
    if (normalizedRoadAddress.isBlank()) return null

    return PlaceSearchResult(
        id = null,
        name = normalizedName,
        category = normalizedCategory,
        roadAddress = normalizedRoadAddress,
        address = normalizedRoadAddress,
        latitude = normalizedLatitude,
        longitude = normalizedLongitude
    )
}
