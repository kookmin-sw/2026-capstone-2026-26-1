package com.example.passedpath.feature.route.presentation.mapper

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.RoutePolylineSegmentUiState
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState
import com.example.passedpath.ui.state.CoordinateUiState

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
    val polylinePoints = points.map(TrackedLocation::toCoordinateUiState)
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
    val polylinePoints = dailyPath?.points?.map(TrackedLocation::toCoordinateUiState).orEmpty()

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
    val polylinePoints = polylinePoints.map(RoutePoint::toCoordinateUiState)
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

internal fun patchRouteNoteSnapshot(
    routeModeUiState: MainRouteModeUiState,
    title: String?,
    memo: String?,
    shouldUpdateTitle: Boolean,
    shouldUpdateMemo: Boolean
): MainRouteModeUiState {
    if (!shouldUpdateTitle && !shouldUpdateMemo) return routeModeUiState

    return routeModeUiState.updateRouteSnapshot { route ->
        route.copy(
            title = if (shouldUpdateTitle) title.orEmpty() else route.title,
            memo = if (shouldUpdateMemo) memo.orEmpty() else route.memo
        )
    }
}

internal fun patchRouteBookmarkSnapshot(
    routeModeUiState: MainRouteModeUiState,
    isBookmarked: Boolean
): MainRouteModeUiState {
    return routeModeUiState.updateRouteSnapshot { route ->
        route.copy(isBookmarked = isBookmarked)
    }
}

private fun MainRouteModeUiState.updateRouteSnapshot(
    transform: (SelectedDayRouteUiState) -> SelectedDayRouteUiState
): MainRouteModeUiState {
    return when (this) {
        is MainRouteModeUiState.Today -> copy(route = transform(route))
        is MainRouteModeUiState.Past -> copy(route = transform(route))
    }
}

private fun List<CoordinateUiState>.toRoutePolylineSegments(): List<RoutePolylineSegmentUiState> {
    if (size < 2) return emptyList()

    return zipWithNext { start, end ->
        RoutePolylineSegmentUiState(
            start = start,
            end = end
        )
    }
}

private fun TrackedLocation.toCoordinateUiState(): CoordinateUiState {
    return CoordinateUiState(
        latitude = latitude,
        longitude = longitude,
        recordedAtEpochMillis = recordedAtEpochMillis
    )
}

private fun RoutePoint.toCoordinateUiState(): CoordinateUiState {
    return CoordinateUiState(
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
