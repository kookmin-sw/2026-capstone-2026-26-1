package com.example.passedpath.feature.place.domain.repository

import com.example.passedpath.feature.place.domain.model.PlaceSearchPage

interface PlaceSearchRepository {
    suspend fun search(
        query: String,
        page: Int = 1
    ): PlaceSearchPage
}
