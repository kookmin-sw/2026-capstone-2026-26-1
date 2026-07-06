package com.example.passedpath.feature.care.domain.model

enum class ProtectedPersonPlaceSourceType {
    AUTO,
    MANUAL
}

enum class ProtectedPersonBookmarkPlaceType {
    HOME,
    COMPANY,
    SCHOOL,
    ETC
}

data class ProtectedPersonVisitedPlace(
    val placeId: Long,
    val placeName: String,
    val source: ProtectedPersonPlaceSourceType,
    val bookmarkType: ProtectedPersonBookmarkPlaceType? = null,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int,
    val startTime: String? = null,
    val endTime: String? = null
)

data class ProtectedPersonVisitedPlaceList(
    val placeCount: Int,
    val places: List<ProtectedPersonVisitedPlace>
)
