package com.example.passedpath.feature.place.data.remote.mapper

import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchItemDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceSearchResponseDto
import com.example.passedpath.feature.place.domain.model.PlaceSearchPage
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult

fun PlaceSearchResponseDto.toPlaceSearchResults(): List<PlaceSearchResult> {
    return places.mapNotNull(PlaceSearchItemDto::toPlaceSearchResult)
}

fun PlaceSearchResponseDto.toPlaceSearchPage(): PlaceSearchPage {
    val mappedPlaces = toPlaceSearchResults()
    return PlaceSearchPage(
        page = page ?: 1,
        size = size ?: mappedPlaces.size,
        isEnd = isEnd ?: true,
        pageableCount = pageableCount ?: mappedPlaces.size,
        places = mappedPlaces
    )
}

private fun PlaceSearchItemDto.toPlaceSearchResult(): PlaceSearchResult? {
    val normalizedName = placeName?.trim().orEmpty()
    val normalizedCategory = category.toLeafCategory()
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

private fun String?.toLeafCategory(): String {
    return this
        ?.split(">")
        ?.map(String::trim)
        ?.lastOrNull(String::isNotBlank)
        .orEmpty()
}
