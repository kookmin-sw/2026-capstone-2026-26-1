package com.example.passedpath.feature.place.data.repository

import com.example.passedpath.feature.place.data.remote.api.PlaceApi
import com.example.passedpath.feature.place.data.remote.mapper.toRegisteredPlace
import com.example.passedpath.feature.place.data.remote.mapper.toRequestDto
import com.example.passedpath.feature.place.data.remote.mapper.toUpdateRequestDto
import com.example.passedpath.feature.place.data.remote.mapper.toUpdatedPlace
import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.RegisteredPlace
import com.example.passedpath.feature.place.domain.model.UpdatedPlace
import com.example.passedpath.feature.place.domain.repository.PlaceRepository

class PlaceRepositoryImpl(
    private val placeApi: PlaceApi
) : PlaceRepository {
    override suspend fun addPlace(dateKey: String, place: PlaceRegistration): RegisteredPlace {
        return placeApi.addPlace(
            date = dateKey,
            request = place.toRequestDto()
        ).toRegisteredPlace()
    }

    override suspend fun updatePlace(dateKey: String, placeId: Long, place: PlaceRegistration): UpdatedPlace {
        return placeApi.updatePlace(
            date = dateKey,
            placeId = placeId,
            request = place.toUpdateRequestDto()
        ).toUpdatedPlace()
    }

    override suspend fun deletePlace(dateKey: String, placeId: Long) {
        placeApi.deletePlace(
            date = dateKey,
            placeId = placeId
        )
    }
}
