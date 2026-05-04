package com.example.passedpath.feature.place.data.repository

import com.example.passedpath.feature.place.data.remote.api.PlaceSearchApi
import com.example.passedpath.feature.place.data.remote.mapper.toPlaceSearchPage
import com.example.passedpath.feature.place.domain.model.PlaceSearchPage
import com.example.passedpath.feature.place.domain.repository.PlaceSearchRepository

class PlaceSearchRepositoryImpl(
    private val api: PlaceSearchApi
) : PlaceSearchRepository {
    override suspend fun search(
        query: String,
        page: Int
    ): PlaceSearchPage {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return PlaceSearchPage(
                page = 0,
                size = 0,
                isEnd = true,
                pageableCount = 0,
                places = emptyList()
            )
        }

        return api.searchPlaces(
            query = normalizedQuery,
            page = page
        ).toPlaceSearchPage()
    }
}
