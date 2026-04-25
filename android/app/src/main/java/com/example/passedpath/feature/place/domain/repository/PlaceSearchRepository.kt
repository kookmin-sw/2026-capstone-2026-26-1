package com.example.passedpath.feature.place.domain.repository

import com.example.passedpath.feature.place.domain.model.PlaceSearchResult

interface PlaceSearchRepository {
    suspend fun search(
        query: String,
        page: Int = 1,
        size: Int = 10
    ): List<PlaceSearchResult>
}
