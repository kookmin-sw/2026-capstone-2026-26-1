package com.example.passedpath.feature.place.data.repository

import com.example.passedpath.feature.place.data.remote.api.PlaceSearchApi
import com.example.passedpath.feature.place.data.remote.mapper.toPlaceSearchResults
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.domain.repository.PlaceSearchRepository

class PlaceSearchRepositoryImpl(
    private val api: PlaceSearchApi
) : PlaceSearchRepository {
    override suspend fun search(
        query: String,
        page: Int,
        size: Int
    ): List<PlaceSearchResult> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return emptyList()

        return api.searchPlaces(
            query = normalizedQuery,
            page = page,
            size = size
        ).toPlaceSearchResults()
    }
}
