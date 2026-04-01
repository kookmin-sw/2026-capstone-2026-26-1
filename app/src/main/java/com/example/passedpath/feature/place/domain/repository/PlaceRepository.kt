package com.example.passedpath.feature.place.domain.repository

import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.RegisteredPlace
import com.example.passedpath.feature.place.domain.model.UpdatedPlace

interface PlaceRepository {
    suspend fun addPlace(dateKey: String, place: PlaceRegistration): RegisteredPlace

    suspend fun updatePlace(dateKey: String, placeId: Long, place: PlaceRegistration): UpdatedPlace

    suspend fun reorderPlaces(dateKey: String, placeIds: List<Long>)

    suspend fun deletePlace(dateKey: String, placeId: Long)
}
