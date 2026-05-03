package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.model.PlaceSearchPage
import com.example.passedpath.feature.place.domain.repository.PlaceSearchRepository

class SearchPlacesUseCase(
    private val repository: PlaceSearchRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int = 1
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

        return repository.search(
            query = normalizedQuery,
            page = page
        )
    }
}
