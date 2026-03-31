package com.example.passedpath.feature.route.presentation.coordinator

import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.RemoteDayRouteResult
import com.example.passedpath.feature.route.presentation.mapper.createInitialRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createLoadingRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastEmptyRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastErrorRouteMode
import com.example.passedpath.feature.route.presentation.mapper.createPastRouteMode
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
        emit(
            RouteLoadState(
                selectedDateKey = dateKey,
                routeModeUiState = createLoadingRouteMode(
                    dateKey = dateKey,
                    isToday = isToday(dateKey)
                )
            )
        )

        if (isToday(dateKey)) {
            dayRouteRepository.observeLocalDayRoute(dateKey).collect { dailyPath ->
                emit(
                    RouteLoadState(
                        selectedDateKey = dateKey,
                        routeModeUiState = if (dailyPath == null) {
                            createTodayEmptyRouteMode(dateKey)
                        } else {
                            createTodayRouteMode(route = dailyPath.toSelectedDayRouteUiState())
                        }
                    )
                )
            }
        } else {
            emit(loadPastRoute(dateKey))
        }
    }

    private suspend fun loadPastRoute(dateKey: String): RouteLoadState {
        return when (val result = dayRouteRepository.fetchRemoteDayRoute(dateKey)) {
            is RemoteDayRouteResult.Success -> {
                val routeDetail = result.routeDetail
                RouteLoadState(
                    selectedDateKey = routeDetail.dateKey,
                    routeModeUiState = createPastRouteMode(
                        route = routeDetail.toSelectedDayRouteUiState()
                    )
                )
            }

            RemoteDayRouteResult.Empty -> {
                RouteLoadState(
                    selectedDateKey = dateKey,
                    routeModeUiState = createPastEmptyRouteMode(dateKey)
                )
            }

            is RemoteDayRouteResult.Error -> {
                RouteLoadState(
                    selectedDateKey = dateKey,
                    routeModeUiState = createPastErrorRouteMode(dateKey)
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
    val routeModeUiState: MainRouteModeUiState
)
