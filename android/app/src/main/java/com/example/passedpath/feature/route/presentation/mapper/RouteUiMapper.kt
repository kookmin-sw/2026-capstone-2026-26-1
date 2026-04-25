package com.example.passedpath.feature.route.presentation.mapper

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.example.passedpath.feature.route.presentation.policy.shouldRenderGapAsDashed
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.RoutePolylineSegmentUiState
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState

internal fun createInitialRouteMode(dateKey: String, isToday: Boolean): MainRouteModeUiState {
    val route = SelectedDayRouteUiState(dateKey = dateKey)
    return if (isToday) {
        createTodayRouteMode(route = route)
    } else {
        createPastRouteMode(route = route)
    }
}

internal fun createLoadingRouteMode(dateKey: String, isToday: Boolean): MainRouteModeUiState {
    val route = SelectedDayRouteUiState(dateKey = dateKey)
    return if (isToday) {
        createTodayRouteMode(
            route = route,
            isRouteLoading = true
        )
    } else {
        createPastRouteMode(
            route = route,
            isRouteLoading = true
        )
    }
}

internal fun createTodayEmptyRouteMode(dateKey: String): MainRouteModeUiState.Today {
    return createTodayRouteMode(
        route = SelectedDayRouteUiState(dateKey = dateKey),
        isRouteEmpty = true,
        routeEmptyMessage = "오늘의 이동 경로가 기록되면 이곳에 표시됩니다."
    )
}

internal fun createPastEmptyRouteMode(dateKey: String): MainRouteModeUiState.Past {
    return createPastRouteMode(
        route = SelectedDayRouteUiState(dateKey = dateKey),
        isRouteEmpty = true,
        routeEmptyMessage = "선택한 날짜에는 지도에 표시할 경로 데이터가 없습니다."
    )
}

internal fun createPastErrorRouteMode(dateKey: String): MainRouteModeUiState.Past {
    return createPastRouteMode(
        route = SelectedDayRouteUiState(dateKey = dateKey),
        routeErrorMessage = "선택한 날짜의 경로를 불러오지 못했습니다."
    )
}

internal fun createTodayRouteMode(
    route: SelectedDayRouteUiState,
    isRouteLoading: Boolean = false,
    isRouteEmpty: Boolean = false,
    routeEmptyMessage: String? = null,
    routeErrorMessage: String? = null
): MainRouteModeUiState.Today {
    return MainRouteModeUiState.Today(
        route = route,
        isRouteLoading = isRouteLoading,
        isRouteEmpty = isRouteEmpty,
        routeEmptyMessage = routeEmptyMessage,
        routeErrorMessage = routeErrorMessage
    )
}

internal fun createPastRouteMode(
    route: SelectedDayRouteUiState,
    isRouteLoading: Boolean = false,
    isRouteEmpty: Boolean = false,
    routeEmptyMessage: String? = null,
    routeErrorMessage: String? = null
): MainRouteModeUiState.Past {
    return MainRouteModeUiState.Past(
        route = route,
        isRouteLoading = isRouteLoading,
        isRouteEmpty = isRouteEmpty,
        routeEmptyMessage = routeEmptyMessage,
        routeErrorMessage = routeErrorMessage
    )
}

internal fun DailyPath.toSelectedDayRouteUiState(): SelectedDayRouteUiState {
    val polylinePoints = points.map(TrackedLocation::toMainCoordinateUiState)
    return SelectedDayRouteUiState(
        dateKey = dateKey,
        title = "",
        memo = "",
        isBookmarked = false,
        polylinePoints = polylinePoints,
        routeSegments = polylinePoints.toRoutePolylineSegments(),
        totalDistanceKm = totalDistanceMeters / 1000.0,
        pathPointCount = pathPointCount,
        markerPlaces = emptyList()
    )
}

internal fun createTodaySelectedDayRouteUiState(
    dateKey: String,
    dailyPath: DailyPath?,
    remoteRouteDetail: DayRouteDetail?
): SelectedDayRouteUiState {
    val remoteRouteUiState = remoteRouteDetail?.toSelectedDayRouteUiState()
    val polylinePoints = dailyPath?.points?.map(TrackedLocation::toMainCoordinateUiState).orEmpty()

    return SelectedDayRouteUiState(
        dateKey = dateKey,
        title = remoteRouteUiState?.title.orEmpty(),
        memo = remoteRouteUiState?.memo.orEmpty(),
        isBookmarked = remoteRouteUiState?.isBookmarked ?: false,
        polylinePoints = polylinePoints,
        routeSegments = polylinePoints.toRoutePolylineSegments(),
        totalDistanceKm = (dailyPath?.totalDistanceMeters ?: 0.0) / 1000.0,
        pathPointCount = dailyPath?.pathPointCount ?: 0,
        markerPlaces = remoteRouteUiState?.markerPlaces.orEmpty()
    )
}

internal fun DayRouteDetail.toSelectedDayRouteUiState(): SelectedDayRouteUiState {
    val polylinePoints = polylinePoints.map(RoutePoint::toMainCoordinateUiState)
    return SelectedDayRouteUiState(
        dateKey = dateKey,
        title = title,
        memo = memo,
        isBookmarked = isBookmarked,
        polylinePoints = polylinePoints,
        routeSegments = polylinePoints.toRoutePolylineSegments(),
        totalDistanceKm = totalDistanceKm,
        pathPointCount = pathPointCount,
        markerPlaces = places.map(DayRoutePlace::toPlaceMarkerUiState)
    )
}

private fun List<MainCoordinateUiState>.toRoutePolylineSegments(): List<RoutePolylineSegmentUiState> {
    if (size < 2) return emptyList()

    return zipWithNext { start, end ->
        RoutePolylineSegmentUiState(
            start = start,
            end = end,
            isDashed = shouldRenderGapAsDashed(start = start, end = end)
        )
    }
}

private fun TrackedLocation.toMainCoordinateUiState(): MainCoordinateUiState {
    return MainCoordinateUiState(
        latitude = latitude,
        longitude = longitude,
        recordedAtEpochMillis = recordedAtEpochMillis
    )
}

private fun RoutePoint.toMainCoordinateUiState(): MainCoordinateUiState {
    return MainCoordinateUiState(
        latitude = latitude,
        longitude = longitude
    )
}

private fun DayRoutePlace.toPlaceMarkerUiState(): PlaceMarkerUiState {
    return PlaceMarkerUiState(
        placeId = placeId,
        placeName = placeName,
        roadAddress = roadAddress,
        latitude = latitude,
        longitude = longitude,
        orderIndex = orderIndex
    )
}
