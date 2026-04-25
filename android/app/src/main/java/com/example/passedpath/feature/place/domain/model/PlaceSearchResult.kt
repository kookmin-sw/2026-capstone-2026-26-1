package com.example.passedpath.feature.place.domain.model

data class PlaceSearchResult(
    val id: String?,
    val name: String,
    val category: String,
    val roadAddress: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayAddress: String
        get() = roadAddress.ifBlank { address }

    val stableKey: String
        get() = id ?: listOf(name, displayAddress, latitude, longitude).joinToString("|")
}
