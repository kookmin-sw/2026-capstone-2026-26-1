package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.route.presentation.state.RouteUiAction

internal sealed interface MainRouteActionRequest {
    data class ReloadRoute(
        val dateKey: String,
        val trigger: RouteReloadTrigger
    ) : MainRouteActionRequest

    data object ToggleTracking : MainRouteActionRequest
    data object OpenPastPlayback : MainRouteActionRequest
}

internal enum class RouteReloadTrigger {
    InitialLoad,
    DateSelection,
    TodayRefresh,
    PastRetry
}

internal fun createRouteReloadRequest(
    dateKey: String,
    trigger: RouteReloadTrigger
): MainRouteActionRequest.ReloadRoute {
    return MainRouteActionRequest.ReloadRoute(
        dateKey = dateKey,
        trigger = trigger
    )
}

internal fun resolveMainRouteActionRequest(
    action: RouteUiAction,
    selectedDateKey: String
): MainRouteActionRequest {
    return when (action) {
        RouteUiAction.RefreshTodayRoute -> createRouteReloadRequest(
            dateKey = selectedDateKey,
            trigger = RouteReloadTrigger.TodayRefresh
        )

        RouteUiAction.RetryPastRoute -> createRouteReloadRequest(
            dateKey = selectedDateKey,
            trigger = RouteReloadTrigger.PastRetry
        )

        RouteUiAction.ToggleTracking -> MainRouteActionRequest.ToggleTracking
        RouteUiAction.EnterPastPlayback -> MainRouteActionRequest.OpenPastPlayback
    }
}
