package com.example.passedpath.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.auth.presentation.screen.LoginRoute
import com.example.passedpath.feature.bookmark.presentation.screen.DayRouteBookmarkListRoute
import com.example.passedpath.feature.auth.presentation.state.AuthEvent
import com.example.passedpath.feature.calendar.presentation.screen.CalendarRoute
import com.example.passedpath.feature.care.presentation.component.CareInviteAcceptDialog
import com.example.passedpath.feature.care.presentation.screen.CareRoute
import com.example.passedpath.feature.care.presentation.screen.ProtectedPersonSummaryDetailRoute
import com.example.passedpath.feature.care.presentation.screen.ProtectedPersonVisitStatisticsDetailRoute
import com.example.passedpath.feature.care.presentation.screen.ProtectedPersonWeeklySummaryRoute
import com.example.passedpath.feature.care.presentation.screen.ProtectedPersonRouteDetailRoute
import com.example.passedpath.feature.care.presentation.screen.ProtectedPersonRouteHistoryRoute
import com.example.passedpath.feature.care.presentation.viewmodel.CareInviteAcceptViewModel
import com.example.passedpath.feature.care.presentation.viewmodel.CareInviteAcceptViewModelFactory
import com.example.passedpath.feature.main.presentation.screen.CalendarDateSelectedEvent
import com.example.passedpath.feature.main.presentation.screen.MainRoute
import com.example.passedpath.feature.main.presentation.screen.PlaceBookmarkChangedEvent
import com.example.passedpath.feature.main.presentation.screen.PlaceCreatedEvent
import com.example.passedpath.feature.permission.presentation.screen.LocationPermissionIntroRoute
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.presentation.screen.AddPlaceScreen
import com.example.passedpath.feature.place.presentation.screen.PlaceBookmarkSearchScreen
import com.example.passedpath.feature.placebookmark.presentation.screen.PlaceBookmarkRoute
import com.example.passedpath.feature.placebookmark.presentation.screen.PlaceBookmarkSearchResultEvent
import com.example.passedpath.feature.summary.presentation.screen.SummaryDetailRoute
import com.example.passedpath.feature.summary.presentation.screen.VisitStatisticsDetailRoute
import com.example.passedpath.feature.summary.presentation.screen.WeeklySummaryRoute
import com.example.passedpath.feature.summary.presentation.state.SummaryDetailMetric
import com.example.passedpath.ui.component.toast.ToastOverlayHost
import com.example.passedpath.ui.component.toast.ToastOverlayItem
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AppNavHost(
    navController: NavHostController,
    appEntryViewModel: AppEntryViewModel,
    pendingCareInviteCode: String? = null,
    onCareInviteCodeConsumed: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val careInviteAcceptViewModel: CareInviteAcceptViewModel = viewModel(
        factory = CareInviteAcceptViewModelFactory(context.appContainer)
    )
    val careInviteAcceptUiState by careInviteAcceptViewModel.uiState
        .collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val inviteAcceptSuccessMessage = stringResource(R.string.care_invite_accept_success)
    var logoutToastMessage by remember { mutableStateOf<String?>(null) }
    var logoutToastTrigger by remember { mutableStateOf(0) }
    var loginToastMessage by remember { mutableStateOf<String?>(null) }
    var loginToastTrigger by remember { mutableStateOf(0) }
    var inviteAcceptToastTrigger by remember { mutableStateOf(0) }
    var handledInviteAcceptSuccessEventId by remember { mutableStateOf(0L) }
    var mainTabReselectionEvent by remember { mutableStateOf(0) }
    var careRefreshEventId by remember { mutableStateOf(0) }
    var placeCreatedEvent by remember { mutableStateOf<PlaceCreatedEvent?>(null) }
    var placeCreatedEventId by remember { mutableStateOf(0) }
    var placeBookmarkChangedEvent by remember { mutableStateOf<PlaceBookmarkChangedEvent?>(null) }
    var placeBookmarkChangedEventId by remember { mutableStateOf(0) }
    var placeBookmarkSearchResultEvent by remember { mutableStateOf<PlaceBookmarkSearchResultEvent?>(null) }
    var placeBookmarkSearchResultEventId by remember { mutableStateOf(0) }
    var calendarDateSelectedEvent by remember { mutableStateOf<CalendarDateSelectedEvent?>(null) }
    var calendarDateSelectedEventId by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        AuthEvent.logoutEvent.collect { event ->
            logoutToastMessage = event.message
            if (event.message != null) {
                logoutToastTrigger++
            }
            navController.navigate(NavRoute.LOGIN) {
                popUpTo(0)
            }
        }
    }

    LaunchedEffect(pendingCareInviteCode, currentRoute) {
        val inviteCode = pendingCareInviteCode
            ?.trim()
            ?.takeIf(String::isNotEmpty) ?: return@LaunchedEffect
        if (!currentRoute.isCareInviteAcceptReadyRoute()) return@LaunchedEffect

        careInviteAcceptViewModel.openInvite(inviteCode)
        onCareInviteCodeConsumed(inviteCode)
    }

    LaunchedEffect(careInviteAcceptUiState.successEventId) {
        val successEventId = careInviteAcceptUiState.successEventId
        if (successEventId <= 0L ||
            successEventId == handledInviteAcceptSuccessEventId
        ) {
            return@LaunchedEffect
        }

        handledInviteAcceptSuccessEventId = successEventId
        inviteAcceptToastTrigger++
        careRefreshEventId++
        navController.navigate(NavRoute.FRIENDS) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavigationGraph(
            navController = navController,
            appEntryViewModel = appEntryViewModel,
            mainTabReselectionEvent = mainTabReselectionEvent,
            careRefreshEventId = careRefreshEventId,
            placeCreatedEvent = placeCreatedEvent,
            placeBookmarkChangedEvent = placeBookmarkChangedEvent,
            placeBookmarkSearchResultEvent = placeBookmarkSearchResultEvent,
            calendarDateSelectedEvent = calendarDateSelectedEvent,
            onPlaceCreatedEventConsumed = { eventId ->
                if (placeCreatedEvent?.id == eventId) {
                    placeCreatedEvent = null
                }
            },
            onPlaceBookmarkSearchResultEventConsumed = { eventId ->
                if (placeBookmarkSearchResultEvent?.id == eventId) {
                    placeBookmarkSearchResultEvent = null
                }
            },
            onPlaceBookmarkChangedEventConsumed = { eventId ->
                if (placeBookmarkChangedEvent?.id == eventId) {
                    placeBookmarkChangedEvent = null
                }
            },
            onCalendarDateSelectedEventConsumed = { eventId ->
                if (calendarDateSelectedEvent?.id == eventId) {
                    calendarDateSelectedEvent = null
                }
            },
            onLoginToastMessage = { message ->
                loginToastMessage = message
                loginToastTrigger++
            },
            onBottomBarReselected = { route ->
                if (route == NavRoute.MAIN) {
                    mainTabReselectionEvent++
                }
            },
            onPlaceCreated = { placeId ->
                placeCreatedEventId++
                placeCreatedEvent = PlaceCreatedEvent(
                    id = placeCreatedEventId,
                    placeId = placeId
                )
            },
            onPlaceBookmarkSearchResult = { place ->
                placeBookmarkSearchResultEventId++
                placeBookmarkSearchResultEvent = PlaceBookmarkSearchResultEvent(
                    id = placeBookmarkSearchResultEventId,
                    place = place
                )
            },
            onPlaceBookmarkChanged = { bookmarkPlaceId ->
                placeBookmarkChangedEventId++
                placeBookmarkChangedEvent = PlaceBookmarkChangedEvent(
                    id = placeBookmarkChangedEventId,
                    bookmarkPlaceId = bookmarkPlaceId
                )
            },
            onCalendarDateSelected = { dateKey ->
                calendarDateSelectedEventId++
                calendarDateSelectedEvent = CalendarDateSelectedEvent(
                    id = calendarDateSelectedEventId,
                    dateKey = dateKey
                )
            },
            modifier = Modifier.fillMaxSize()
        )

        ToastOverlayHost(
            toasts = buildList {
                logoutToastMessage?.let { message ->
                    add(
                        ToastOverlayItem(
                            message = message,
                            triggerKey = "logout:$logoutToastTrigger:$message"
                        )
                    )
                }
                loginToastMessage?.let { message ->
                    add(
                        ToastOverlayItem(
                            message = message,
                            triggerKey = "login:$loginToastTrigger:$message"
                        )
                    )
                }
                if (inviteAcceptToastTrigger > 0) {
                    add(
                        ToastOverlayItem(
                            message = inviteAcceptSuccessMessage,
                            triggerKey = "care-invite-accept:$inviteAcceptToastTrigger"
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        CareInviteAcceptDialog(
            uiState = careInviteAcceptUiState,
            onDismiss = careInviteAcceptViewModel::dismissInvite,
            onConfirm = careInviteAcceptViewModel::acceptInvite
        )
    }
}

@Composable
private fun AppNavigationGraph(
    navController: NavHostController,
    appEntryViewModel: AppEntryViewModel,
    mainTabReselectionEvent: Int,
    careRefreshEventId: Int,
    placeCreatedEvent: PlaceCreatedEvent?,
    placeBookmarkChangedEvent: PlaceBookmarkChangedEvent?,
    placeBookmarkSearchResultEvent: PlaceBookmarkSearchResultEvent?,
    calendarDateSelectedEvent: CalendarDateSelectedEvent?,
    onPlaceCreatedEventConsumed: (Int) -> Unit,
    onPlaceBookmarkSearchResultEventConsumed: (Int) -> Unit,
    onPlaceBookmarkChangedEventConsumed: (Int) -> Unit,
    onCalendarDateSelectedEventConsumed: (Int) -> Unit,
    onLoginToastMessage: (String) -> Unit,
    onBottomBarReselected: (String) -> Unit,
    onPlaceCreated: (Long) -> Unit,
    onPlaceBookmarkSearchResult: (PlaceSearchResult) -> Unit,
    onPlaceBookmarkChanged: (Long) -> Unit,
    onCalendarDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.ENTRY,
        modifier = modifier
    ) {
        composable(NavRoute.ENTRY) {
            AppEntryRoute(
                viewModel = appEntryViewModel,
                onResolved = { destination ->
                    navController.navigate(destination) {
                        popUpTo(NavRoute.ENTRY) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.LOGIN) {
            LoginRoute(
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo(NavRoute.LOGIN) { inclusive = true }
                    }
                },
                onShowToastMessage = onLoginToastMessage
            )
        }

        composable(NavRoute.PERMISSION_INTRO) {
            LocationPermissionIntroRoute(
                onPermissionResolved = {
                    navController.navigate(NavRoute.MAIN) {
                        popUpTo(NavRoute.PERMISSION_INTRO) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.FRIENDS) {
            BottomBarScaffold(
                navController = navController,
                selectedRoute = NavRoute.FRIENDS,
                onBottomBarReselected = onBottomBarReselected
            ) { modifier ->
                CareRoute(
                    refreshEventId = careRefreshEventId,
                    onNavigateToProtectedPersonRouteHistory = { dependentUserId, nickname ->
                        navController.navigate(
                            NavRoute.careRouteHistory(
                                dependentUserId = dependentUserId,
                                nickname = nickname
                            )
                        )
                    },
                    onNavigateToProtectedPersonWeeklySummary = { dependentUserId, nickname ->
                        navController.navigate(
                            NavRoute.careWeeklySummary(
                                dependentUserId = dependentUserId,
                                nickname = nickname
                            )
                        )
                    },
                    modifier = modifier
                )
            }
        }

        composable(
            route = NavRoute.CARE_WEEKLY_SUMMARY_WITH_ARGS,
            arguments = listOf(
                navArgument(NavRoute.CARE_WEEKLY_SUMMARY_DEPENDENT_USER_ID) {
                    type = NavType.LongType
                },
                navArgument(NavRoute.CARE_WEEKLY_SUMMARY_NICKNAME) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) { backStackEntry ->
            val dependentUserId = backStackEntry.arguments
                ?.getLong(NavRoute.CARE_WEEKLY_SUMMARY_DEPENDENT_USER_ID)
                ?: return@composable
            val nickname = backStackEntry.arguments
                ?.getString(NavRoute.CARE_WEEKLY_SUMMARY_NICKNAME)
                .orEmpty()

            ProtectedPersonWeeklySummaryRoute(
                dependentUserId = dependentUserId,
                dependentNickname = nickname,
                onBackClick = {
                    navController.popBackStack()
                },
                onMetricClick = { metric ->
                    navController.navigate(
                        NavRoute.careSummaryDetail(
                            dependentUserId = dependentUserId,
                            metric = metric.routeValue
                        )
                    )
                }
            )
        }

        composable(
            route = NavRoute.CARE_SUMMARY_DETAIL_WITH_ARGS,
            arguments = listOf(
                navArgument(NavRoute.CARE_SUMMARY_DETAIL_DEPENDENT_USER_ID) {
                    type = NavType.LongType
                },
                navArgument(NavRoute.CARE_SUMMARY_DETAIL_METRIC_KEY) {
                    type = NavType.StringType
                }
            ),
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) { backStackEntry ->
            val dependentUserId = backStackEntry.arguments
                ?.getLong(NavRoute.CARE_SUMMARY_DETAIL_DEPENDENT_USER_ID)
                ?: return@composable
            val metric = SummaryDetailMetric.fromRouteValue(
                backStackEntry.arguments?.getString(NavRoute.CARE_SUMMARY_DETAIL_METRIC_KEY)
            )

            if (metric == SummaryDetailMetric.VISITS) {
                ProtectedPersonVisitStatisticsDetailRoute(
                    dependentUserId = dependentUserId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                ProtectedPersonSummaryDetailRoute(
                    dependentUserId = dependentUserId,
                    metric = metric,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = NavRoute.CARE_ROUTE_HISTORY_WITH_ARGS,
            arguments = listOf(
                navArgument(NavRoute.CARE_ROUTE_HISTORY_DEPENDENT_USER_ID) {
                    type = NavType.LongType
                },
                navArgument(NavRoute.CARE_ROUTE_HISTORY_NICKNAME) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) { backStackEntry ->
            val dependentUserId = backStackEntry.arguments
                ?.getLong(NavRoute.CARE_ROUTE_HISTORY_DEPENDENT_USER_ID)
                ?: return@composable
            val nickname = backStackEntry.arguments
                ?.getString(NavRoute.CARE_ROUTE_HISTORY_NICKNAME)
                .orEmpty()

            BottomBarScaffold(
                navController = navController,
                selectedRoute = NavRoute.FRIENDS,
                onBottomBarReselected = onBottomBarReselected
            ) { modifier ->
                ProtectedPersonRouteHistoryRoute(
                    dependentUserId = dependentUserId,
                    dependentNickname = nickname,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRouteDateClick = { dateKey ->
                        navController.navigate(
                            NavRoute.careRouteDetail(
                                dependentUserId = dependentUserId,
                                dateKey = dateKey,
                                nickname = nickname
                            )
                        )
                    },
                    modifier = modifier
                )
            }
        }

        composable(
            route = NavRoute.CARE_ROUTE_DETAIL_WITH_ARGS,
            arguments = listOf(
                navArgument(NavRoute.CARE_ROUTE_DETAIL_DEPENDENT_USER_ID) {
                    type = NavType.LongType
                },
                navArgument(NavRoute.CARE_ROUTE_DETAIL_DATE_KEY) {
                    type = NavType.StringType
                },
                navArgument(NavRoute.CARE_ROUTE_DETAIL_NICKNAME) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) { backStackEntry ->
            val dependentUserId = backStackEntry.arguments
                ?.getLong(NavRoute.CARE_ROUTE_DETAIL_DEPENDENT_USER_ID)
                ?: return@composable
            val dateKey = backStackEntry.arguments
                ?.getString(NavRoute.CARE_ROUTE_DETAIL_DATE_KEY)
                .orEmpty()
            val nickname = backStackEntry.arguments
                ?.getString(NavRoute.CARE_ROUTE_DETAIL_NICKNAME)
                .orEmpty()

            BottomBarScaffold(
                navController = navController,
                selectedRoute = NavRoute.FRIENDS,
                onBottomBarReselected = onBottomBarReselected
            ) { modifier ->
                ProtectedPersonRouteDetailRoute(
                    dependentUserId = dependentUserId,
                    dependentNickname = nickname,
                    dateKey = dateKey,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    modifier = modifier
                )
            }
        }

        composable(NavRoute.MAIN) {
            BottomBarScaffold(
                navController = navController,
                selectedRoute = NavRoute.MAIN,
                onBottomBarReselected = onBottomBarReselected
            ) { modifier ->
                Box(modifier = modifier) {
                    MainRoute(
                        mainTabReselectionEvent = mainTabReselectionEvent,
                        placeCreatedEvent = placeCreatedEvent,
                        placeBookmarkChangedEvent = placeBookmarkChangedEvent,
                        calendarDateSelectedEvent = calendarDateSelectedEvent,
                        onPlaceCreatedEventConsumed = onPlaceCreatedEventConsumed,
                        onPlaceBookmarkChangedEventConsumed = onPlaceBookmarkChangedEventConsumed,
                        onCalendarDateSelectedEventConsumed = onCalendarDateSelectedEventConsumed,
                        onNavigateToAddPlace = { dateKey ->
                            navController.navigate(NavRoute.addPlace(dateKey))
                        },
                        onNavigateToPlaceBookmarks = {
                            navController.navigate(NavRoute.PLACE_BOOKMARKS)
                        },
                        onNavigateToCalendar = { dateKey ->
                            navController.navigate(NavRoute.calendar(dateKey))
                        },
                        onNavigateToWeeklySummary = {
                            navController.navigate(NavRoute.WEEKLY_SUMMARY)
                        },
                        onNavigateToSummaryDetail = { metric, dateKey ->
                            navController.navigate(
                                NavRoute.summaryDetail(
                                    metric = metric.routeValue,
                                    dateKey = dateKey
                                )
                            )
                        }
                    )
                }
            }
        }

        composable(
            route = NavRoute.WEEKLY_SUMMARY,
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) {
            WeeklySummaryRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onMetricClick = { metric ->
                    navController.navigate(
                        NavRoute.summaryDetail(
                            metric = metric.routeValue,
                            dateKey = currentSummaryDetailDateKey()
                        )
                    )
                }
            )
        }

        composable(
            route = NavRoute.SUMMARY_DETAIL_WITH_ARGS,
            arguments = listOf(
                navArgument(NavRoute.SUMMARY_DETAIL_METRIC_KEY) {
                    type = NavType.StringType
                },
                navArgument(NavRoute.SUMMARY_DETAIL_DATE_KEY) {
                    type = NavType.StringType
                }
            ),
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) { backStackEntry ->
            val metric = SummaryDetailMetric.fromRouteValue(
                backStackEntry.arguments?.getString(NavRoute.SUMMARY_DETAIL_METRIC_KEY)
            )
            val dateKey = backStackEntry.arguments
                ?.getString(NavRoute.SUMMARY_DETAIL_DATE_KEY)
                .orEmpty()

            if (metric == SummaryDetailMetric.VISITS) {
                VisitStatisticsDetailRoute(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                SummaryDetailRoute(
                    metric = metric,
                    dateKey = dateKey,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = NavRoute.CALENDAR_WITH_DATE,
            arguments = listOf(
                navArgument(NavRoute.CALENDAR_DATE_KEY) {
                    type = NavType.StringType
                }
            ),
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) { backStackEntry ->
            val dateKey = backStackEntry.arguments
                ?.getString(NavRoute.CALENDAR_DATE_KEY)
                .orEmpty()

            CalendarRoute(
                initialDateKey = dateKey,
                onBackClick = {
                    navController.popBackStack()
                },
                onDateConfirmed = { selectedDateKey ->
                    onCalendarDateSelected(selectedDateKey)
                    navController.popBackStack()
                },
                onFavoriteListClick = {
                    navController.navigate(NavRoute.DAY_ROUTE_BOOKMARKS)
                }
            )
        }

        composable(
            route = NavRoute.DAY_ROUTE_BOOKMARKS,
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) {
            DayRouteBookmarkListRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onBookmarkClick = { dateKey ->
                    onCalendarDateSelected(dateKey)
                    navController.popBackStack(
                        route = NavRoute.MAIN,
                        inclusive = false
                    )
                }
            )
        }

        composable(
            route = NavRoute.ADD_PLACE_WITH_DATE,
            arguments = listOf(
                navArgument(NavRoute.ADD_PLACE_DATE_KEY) {
                    type = NavType.StringType
                }
            ),
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) { backStackEntry ->
            val dateKey = backStackEntry.arguments
                ?.getString(NavRoute.ADD_PLACE_DATE_KEY)
                .orEmpty()

            AddPlaceScreen(
                dateKey = dateKey,
                onBackClick = {
                    navController.popBackStack()
                },
                onPlaceCreated = { placeId ->
                    onPlaceCreated(placeId)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavRoute.PLACE_BOOKMARKS,
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) {
            PlaceBookmarkRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToPlaceBookmarkSearch = {
                    navController.navigate(NavRoute.PLACE_BOOKMARK_SEARCH)
                },
                searchResultEvent = placeBookmarkSearchResultEvent,
                onSearchResultEventConsumed = onPlaceBookmarkSearchResultEventConsumed,
                onPlaceBookmarkChanged = onPlaceBookmarkChanged
            )
        }

        composable(
            route = NavRoute.PLACE_BOOKMARK_SEARCH,
            enterTransition = { placeSearchEnterTransition() },
            popExitTransition = { placeSearchPopExitTransition() }
        ) {
            PlaceBookmarkSearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPlaceSelected = { place ->
                    onPlaceBookmarkSearchResult(place)
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoute.MYPAGE) {
            LaunchedEffect(Unit) {
                navController.navigate(NavRoute.MAIN) {
                    popUpTo(NavRoute.MYPAGE) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.placeSearchEnterTransition(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(durationMillis = PlaceSearchEnterTransitionMillis)
    ) + fadeIn(animationSpec = tween(durationMillis = PlaceSearchEnterTransitionMillis))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.placeSearchPopExitTransition(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(durationMillis = PlaceSearchExitTransitionMillis)
    ) + fadeOut(animationSpec = tween(durationMillis = PlaceSearchExitTransitionMillis))
}

private const val PlaceSearchEnterTransitionMillis = 250
private const val PlaceSearchExitTransitionMillis = 230
private const val SummaryDetailZoneId = "Asia/Seoul"

private fun currentSummaryDetailDateKey(): String {
    return LocalDate.now(ZoneId.of(SummaryDetailZoneId)).toString()
}

private fun String?.isCareInviteAcceptReadyRoute(): Boolean {
    return this != null &&
        this != NavRoute.ENTRY &&
        this != NavRoute.LOGIN &&
        this != NavRoute.PERMISSION_INTRO
}
