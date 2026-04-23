package com.example.passedpath.feature.place.domain.usecase

import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.domain.repository.PlaceSearchRepository

class SearchPlacesUseCase(
    private val repository: PlaceSearchRepository
) {
    suspend operator fun invoke(query: String): List<PlaceSearchResult> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return emptyList()

        return repository.search(normalizedQuery)
    }
}
