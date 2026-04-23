package com.example.passedpath.feature.place.data.remote.mapper

import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchItemDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchResponseDto
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult

fun PlaceSearchResponseDto.toPlaceSearchResults(): List<PlaceSearchResult> {
    return places.mapNotNull(PlaceSearchItemDto::toPlaceSearchResult)
}

private fun PlaceSearchItemDto.toPlaceSearchResult(): PlaceSearchResult? {
    val normalizedName = name?.trim().orEmpty()
    val normalizedCategory = category?.trim().orEmpty()
    val normalizedRoadAddress = roadAddress?.trim().orEmpty()
    val normalizedAddress = address?.trim().orEmpty()
    val normalizedLatitude = latitude ?: return null
    val normalizedLongitude = longitude ?: return null

    if (normalizedName.isBlank()) return null
    if (normalizedRoadAddress.isBlank() && normalizedAddress.isBlank()) return null

    return PlaceSearchResult(
        id = id?.trim()?.takeIf(String::isNotBlank),
        name = normalizedName,
        category = normalizedCategory,
        roadAddress = normalizedRoadAddress,
        address = normalizedAddress,
        latitude = normalizedLatitude,
        longitude = normalizedLongitude
    )
}
