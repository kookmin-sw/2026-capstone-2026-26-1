package com.example.passedpath.feature.route.presentation.coordinator

import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.LocalDayRouteSnapshot

internal fun createRouteLoadingDebugSnapshot(isTodayRoute: Boolean, dateKey: String): RouteDebugSnapshot {
    return RouteDebugSnapshot(
        source = if (isTodayRoute) "local" else "remote",
        status = "loading",
        message = "dateKey=$dateKey"
    )
}

internal fun createTodayRouteDebugSnapshot(routeSnapshot: LocalDayRouteSnapshot?): RouteDebugSnapshot {
    return if (routeSnapshot == null) {
        RouteDebugSnapshot(
            source = "local",
            status = "empty",
            message = "no local route data"
        )
    } else {
        RouteDebugSnapshot(
            source = "local",
            status = "success",
            message = "points=${routeSnapshot.pathPointCount} distanceKm=${routeSnapshot.totalDistanceMeters / 1000.0}"
        )
    }
}

internal fun createPastRouteSuccessDebugSnapshot(routeDetail: DayRouteDetail): RouteDebugSnapshot {
    return RouteDebugSnapshot(
        source = "remote",
        status = "success",
        message = "points=${routeDetail.pathPointCount} decoded=${routeDetail.polylinePoints.size} distanceKm=${routeDetail.totalDistanceKm}"
    )
}

internal fun createPastRouteEmptyDebugSnapshot(): RouteDebugSnapshot {
    return RouteDebugSnapshot(
        source = "remote",
        status = "empty",
        message = "no remote route data"
    )
}

internal fun createPastRouteErrorDebugSnapshot(throwable: Throwable): RouteDebugSnapshot {
    return RouteDebugSnapshot(
        source = "remote",
        status = "error",
        message = throwable::class.java.simpleName
    )
}
