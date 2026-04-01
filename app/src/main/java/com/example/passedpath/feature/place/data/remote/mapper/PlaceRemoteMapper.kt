package com.example.passedpath.feature.place.data.remote.mapper

import com.example.passedpath.feature.place.data.remote.dto.PlaceAddRequestDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceAddResponseDto
import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.RegisteredPlace

internal fun PlaceRegistration.toRequestDto(): PlaceAddRequestDto {
    return PlaceAddRequestDto(
        roadAddress = roadAddress,
        placeName = placeName,
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
