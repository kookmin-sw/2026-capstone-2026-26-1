package com.example.passedpath.feature.place.domain.repository

import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.RegisteredPlace

interface PlaceRepository {
    suspend fun addPlace(dateKey: String, place: PlaceRegistration): RegisteredPlace

    suspend fun deletePlace(dateKey: String, placeId: Long)
}
