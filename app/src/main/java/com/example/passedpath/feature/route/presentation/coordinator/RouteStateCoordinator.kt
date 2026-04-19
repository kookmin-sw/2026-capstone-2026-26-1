package com.example.passedpath.feature.route.presentation.coordinator

import com.example.passedpath.debug.AppDebugLogger
import com.example.passedpath.debug.DebugLogTag
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.route.presentation.mapper.createInitialRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createLoadingRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastEmptyRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastErrorRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createTodaySelectedDayRouteUiState
import com.example.passedpath.feature.route.presentation.mapper.createTodayEmptyRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createTodayRouteMode
import com.example.passedpath.feature.route.presentation.mapper.toSelectedDayRouteUiState
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RouteStateCoordinator(
    private val dayRouteRepository: DayRouteRepository,
    private val todayDateKeyProvider: () -> String
) {

    fun createInitialState(dateKey: String): MainRouteModeUiState {
        return createInitialRouteMode(
            dateKey = dateKey,
            isToday = isToday(dateKey)
        )
    }

    fun loadRoute(dateKey: String): Flow<RouteLoadState> = flow {
        val isTodayRoute = isToday(dateKey)
        AppDebugLogger.debug(
            DebugLogTag.ROUTE_LOAD,
            "loadRoute start dateKey=$dateKey mode=${if (isTodayRoute) "today" else "past"}"
        )
        emit(
            RouteLoadState(
                selectedDateKey = dateKey,
                routeModeUiState = createLoadingRouteMode(
                    dateKey = dateKey,
                    isToday = isTodayRoute
                ),
                debugSnapshot = createRouteLoadingDebugSnapshot(
                    isTodayRoute = isTodayRoute,
                    dateKey = dateKey
                )
            )
        )

        if (isTodayRoute) {
            val remoteRouteDetail = when (val remoteResult = dayRouteRepository.fetchRemoteDayRoute(dateKey)) {
                is RemoteDayRouteResult.Success -> remoteResult.routeDetail
                RemoteDayRouteResult.Empty -> null
                is RemoteDayRouteResult.Error -> null
            }
            AppDebugLogger.debug(
                DebugLogTag.ROUTE_LOAD,
                "observe local route dateKey=$dateKey"
            )
            dayRouteRepository.observeLocalDayRoute(dateKey).collect { dailyPath ->
                val hasRemoteReadData = remoteRouteDetail != null
                val routeState = if (dailyPath == null && !hasRemoteReadData) {
                    AppDebugLogger.debug(
                        DebugLogTag.ROUTE_LOAD,
                        "local route empty dateKey=$dateKey"
                    )
                    RouteLoadState(
                        selectedDateKey = dateKey,
                        routeModeUiState = createTodayEmptyRouteMode(dateKey),
                        debugSnapshot = createTodayRouteDebugSnapshot(dailyPath)
                    )
                } else {
                    AppDebugLogger.debug(
                        DebugLogTag.ROUTE_LOAD,
                        "today route success dateKey=$dateKey localPoints=${dailyPath?.pathPointCount ?: 0} remoteSeed=$hasRemoteReadData"
                    )
                    RouteLoadState(
                        selectedDateKey = dateKey,
                        routeModeUiState = createTodayRouteMode(
                            route = createTodaySelectedDayRouteUiState(
                                dateKey = dateKey,
                                dailyPath = dailyPath,
                                remoteRouteDetail = remoteRouteDetail
                            )
                        ),
                        debugSnapshot = createTodayRouteDebugSnapshot(dailyPath)
                    )
                }
                emit(routeState)
            }
        } else {
            emit(loadPastRoute(dateKey))
        }
    }

    private suspend fun loadPastRoute(dateKey: String): RouteLoadState {
        return when (val result = dayRouteRepository.fetchRemoteDayRoute(dateKey)) {
            is RemoteDayRouteResult.Success -> {
                val routeDetail = result.routeDetail
                AppDebugLogger.debug(
                    DebugLogTag.ROUTE_LOAD,
                    "remote route success dateKey=$dateKey points=${routeDetail.pathPointCount} decodedPoints=${routeDetail.polylinePoints.size} encodedLength=${routeDetail.encodedPath.length}"
                )
                RouteLoadState(
                    selectedDateKey = routeDetail.dateKey,
                    routeModeUiState = createPastRouteMode(
                        route = routeDetail.toSelectedDayRouteUiState()
                    ),
                    debugSnapshot = createPastRouteSuccessDebugSnapshot(routeDetail)
                )
            }

            RemoteDayRouteResult.Empty -> {
                AppDebugLogger.debug(
                    DebugLogTag.ROUTE_LOAD,
                    "remote route empty dateKey=$dateKey"
                )
                RouteLoadState(
                    selectedDateKey = dateKey,
                    routeModeUiState = createPastEmptyRouteMode(dateKey),
                    debugSnapshot = createPastRouteEmptyDebugSnapshot()
                )
            }

            is RemoteDayRouteResult.Error -> {
                AppDebugLogger.debug(
                    DebugLogTag.ROUTE_LOAD,
                    "remote route error dateKey=$dateKey cause=${result.throwable::class.java.simpleName}"
                )
                RouteLoadState(
                    selectedDateKey = dateKey,
                    routeModeUiState = createPastErrorRouteMode(dateKey),
                    debugSnapshot = createPastRouteErrorDebugSnapshot(result.throwable)
                )
            }
        }
    }

    private fun isToday(dateKey: String): Boolean {
        return dateKey == todayDateKeyProvider()
    }
}

data class RouteLoadState(
    val selectedDateKey: String,
    val routeModeUiState: MainRouteModeUiState,
    val debugSnapshot: RouteDebugSnapshot? = null
)
