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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.passedpath.feature.auth.presentation.screen.LoginRoute
import com.example.passedpath.feature.auth.presentation.state.AuthEvent
import com.example.passedpath.feature.friends.presentation.screen.FriendsRoute
import com.example.passedpath.feature.main.presentation.screen.MainRoute
import com.example.passedpath.feature.main.presentation.screen.PlaceCreatedEvent
import com.example.passedpath.feature.mypage.presentation.screen.MyPageRoute
import com.example.passedpath.feature.permission.presentation.screen.LocationPermissionIntroRoute
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.presentation.screen.AddPlaceScreen
import com.example.passedpath.feature.place.presentation.screen.PlaceBookmarkSearchScreen
import com.example.passedpath.feature.placebookmark.presentation.screen.PlaceBookmarkRoute
import com.example.passedpath.feature.placebookmark.presentation.screen.PlaceBookmarkSearchResultEvent
import com.example.passedpath.ui.component.toast.ToastOverlayHost
import com.example.passedpath.ui.component.toast.ToastOverlayItem

@Composable
fun AppNavHost(
    navController: NavHostController,
    appEntryViewModel: AppEntryViewModel
) {
    var logoutToastMessage by remember { mutableStateOf<String?>(null) }
    var logoutToastTrigger by remember { mutableStateOf(0) }
    var loginToastMessage by remember { mutableStateOf<String?>(null) }
    var loginToastTrigger by remember { mutableStateOf(0) }
    var mainTabReselectionEvent by remember { mutableStateOf(0) }
    var placeCreatedEvent by remember { mutableStateOf<PlaceCreatedEvent?>(null) }
    var placeCreatedEventId by remember { mutableStateOf(0) }
    var placeBookmarkSearchResultEvent by remember { mutableStateOf<PlaceBookmarkSearchResultEvent?>(null) }
    var placeBookmarkSearchResultEventId by remember { mutableStateOf(0) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavigationGraph(
            navController = navController,
            appEntryViewModel = appEntryViewModel,
            mainTabReselectionEvent = mainTabReselectionEvent,
            placeCreatedEvent = placeCreatedEvent,
            placeBookmarkSearchResultEvent = placeBookmarkSearchResultEvent,
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
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun AppNavigationGraph(
    navController: NavHostController,
    appEntryViewModel: AppEntryViewModel,
    mainTabReselectionEvent: Int,
    placeCreatedEvent: PlaceCreatedEvent?,
    placeBookmarkSearchResultEvent: PlaceBookmarkSearchResultEvent?,
    onPlaceCreatedEventConsumed: (Int) -> Unit,
    onPlaceBookmarkSearchResultEventConsumed: (Int) -> Unit,
    onLoginToastMessage: (String) -> Unit,
    onBottomBarReselected: (String) -> Unit,
    onPlaceCreated: (Long) -> Unit,
    onPlaceBookmarkSearchResult: (PlaceSearchResult) -> Unit,
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
                Box(modifier = modifier) {
                    FriendsRoute()
                }
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
                        onPlaceCreatedEventConsumed = onPlaceCreatedEventConsumed,
                        onNavigateToAddPlace = { dateKey ->
                            navController.navigate(NavRoute.addPlace(dateKey))
                        },
                        onNavigateToPlaceBookmarks = {
                            navController.navigate(NavRoute.PLACE_BOOKMARKS)
                        }
                    )
                }
            }
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
                onSearchResultEventConsumed = onPlaceBookmarkSearchResultEventConsumed
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
            BottomBarScaffold(
                navController = navController,
                selectedRoute = NavRoute.MYPAGE,
                onBottomBarReselected = onBottomBarReselected
            ) { modifier ->
                Box(modifier = modifier) {
                    MyPageRoute()
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
