package com.example.passedpath.feature.place.data.repository

import com.example.passedpath.feature.place.data.remote.api.PlaceApi
import com.example.passedpath.feature.place.data.remote.dto.PlaceErrorResponseDto
import com.example.passedpath.feature.place.data.remote.dto.PlaceReorderRequestDto
import com.example.passedpath.feature.place.data.remote.mapper.toBookmarkPlace
import com.example.passedpath.feature.place.data.remote.mapper.toRegisteredPlace
import com.example.passedpath.feature.place.data.remote.mapper.toVisitedPlaceList
import com.example.passedpath.feature.place.data.remote.mapper.toRequestDto
import com.example.passedpath.feature.place.data.remote.mapper.toUpdateBookmarkPlaceRequestDto
import com.example.passedpath.feature.place.data.remote.mapper.toUpdateRequestDto
import com.example.passedpath.feature.place.data.remote.mapper.toUpdatedPlace
import com.example.passedpath.feature.place.domain.model.BookmarkPlace
import com.example.passedpath.feature.place.domain.model.PlaceRegistration
import com.example.passedpath.feature.place.domain.model.RegisteredPlace
import com.example.passedpath.feature.place.domain.model.UpdatedPlace
import com.example.passedpath.feature.place.domain.model.VisitedPlaceList
import com.example.passedpath.feature.place.domain.repository.PlaceRepository
import com.google.gson.Gson
import retrofit2.HttpException

class PlaceRepositoryImpl(
    private val placeApi: PlaceApi
) : PlaceRepository {
    override suspend fun getPlaces(dateKey: String): VisitedPlaceList {
        return try {
            placeApi.getPlaces(date = dateKey).toVisitedPlaceList()
        } catch (throwable: Throwable) {
            if (throwable.isDayRouteNotFound()) {
                VisitedPlaceList(
                    placeCount = 0,
                    places = emptyList()
                )
            } else {
                throw throwable
            }
        }
    }

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

    override suspend fun reorderPlaces(dateKey: String, placeIds: List<Long>) {
        val response = placeApi.reorderPlaces(
            date = dateKey,
            request = PlaceReorderRequestDto(placeIds = placeIds)
        )
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    override suspend fun updateBookmarkPlace(bookmarkPlaceId: Long, bookmarkPlace: BookmarkPlace): BookmarkPlace {
        return placeApi.updateBookmarkPlace(
            bookmarkPlaceId = bookmarkPlaceId,
            request = bookmarkPlace.toUpdateBookmarkPlaceRequestDto()
        ).toBookmarkPlace()
    }

    override suspend fun deletePlace(dateKey: String, placeId: Long) {
        val response = placeApi.deletePlace(
            date = dateKey,
            placeId = placeId
        )
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
}

private fun Throwable.isDayRouteNotFound(): Boolean {
    val httpException = this as? HttpException ?: return false
    if (httpException.code() != 404) return false

    val errorBody = httpException.response()?.errorBody()?.string()
    val errorResponse = runCatching {
        Gson().fromJson(errorBody, PlaceErrorResponseDto::class.java)
    }.getOrNull()

    return errorResponse?.code == "DAY_ROUTE_NOT_FOUND"
}
