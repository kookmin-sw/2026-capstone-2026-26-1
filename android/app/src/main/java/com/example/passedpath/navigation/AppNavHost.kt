package com.example.passedpath.navigation

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
import com.example.passedpath.feature.main.presentation.screen.PlaceEditSearchCancelledEvent
import com.example.passedpath.feature.main.presentation.screen.PlaceEditSearchResultEvent
import com.example.passedpath.feature.mypage.presentation.screen.MyPageRoute
import com.example.passedpath.feature.permission.presentation.screen.LocationPermissionIntroRoute
import com.example.passedpath.feature.place.domain.model.PlaceSearchResult
import com.example.passedpath.feature.place.presentation.screen.AddPlaceScreen
import com.example.passedpath.feature.place.presentation.screen.EditPlaceSearchScreen
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
    var placeEditSearchResultEvent by remember { mutableStateOf<PlaceEditSearchResultEvent?>(null) }
    var placeEditSearchResultEventId by remember { mutableStateOf(0) }
    var placeEditSearchCancelledEvent by remember { mutableStateOf<PlaceEditSearchCancelledEvent?>(null) }
    var placeEditSearchCancelledEventId by remember { mutableStateOf(0) }

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
            placeEditSearchResultEvent = placeEditSearchResultEvent,
            placeEditSearchCancelledEvent = placeEditSearchCancelledEvent,
            onPlaceCreatedEventConsumed = { eventId ->
                if (placeCreatedEvent?.id == eventId) {
                    placeCreatedEvent = null
                }
            },
            onPlaceEditSearchResultEventConsumed = { eventId ->
                if (placeEditSearchResultEvent?.id == eventId) {
                    placeEditSearchResultEvent = null
                }
            },
            onPlaceEditSearchCancelledEventConsumed = { eventId ->
                if (placeEditSearchCancelledEvent?.id == eventId) {
                    placeEditSearchCancelledEvent = null
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
            onPlaceEditSearchResult = { place ->
                placeEditSearchResultEventId++
                placeEditSearchResultEvent = PlaceEditSearchResultEvent(
                    id = placeEditSearchResultEventId,
                    place = place
                )
            },
            onPlaceEditSearchCancelled = {
                placeEditSearchCancelledEventId++
                placeEditSearchCancelledEvent = PlaceEditSearchCancelledEvent(
                    id = placeEditSearchCancelledEventId
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
    placeEditSearchResultEvent: PlaceEditSearchResultEvent?,
    placeEditSearchCancelledEvent: PlaceEditSearchCancelledEvent?,
    onPlaceCreatedEventConsumed: (Int) -> Unit,
    onPlaceEditSearchResultEventConsumed: (Int) -> Unit,
    onPlaceEditSearchCancelledEventConsumed: (Int) -> Unit,
    onLoginToastMessage: (String) -> Unit,
    onBottomBarReselected: (String) -> Unit,
    onPlaceCreated: (Long) -> Unit,
    onPlaceEditSearchResult: (PlaceSearchResult) -> Unit,
    onPlaceEditSearchCancelled: () -> Unit,
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
                        placeEditSearchResultEvent = placeEditSearchResultEvent,
                        placeEditSearchCancelledEvent = placeEditSearchCancelledEvent,
                        onPlaceCreatedEventConsumed = onPlaceCreatedEventConsumed,
                        onPlaceEditSearchResultEventConsumed = onPlaceEditSearchResultEventConsumed,
                        onPlaceEditSearchCancelledEventConsumed = onPlaceEditSearchCancelledEventConsumed,
                        onNavigateToAddPlace = { dateKey ->
                            navController.navigate(NavRoute.addPlace(dateKey))
                        },
                        onNavigateToEditPlaceSearch = { dateKey ->
                            navController.navigate(NavRoute.editPlaceSearch(dateKey))
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
            )
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
            route = NavRoute.EDIT_PLACE_SEARCH_WITH_DATE,
            arguments = listOf(
                navArgument(NavRoute.EDIT_PLACE_SEARCH_DATE_KEY) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val dateKey = backStackEntry.arguments
                ?.getString(NavRoute.EDIT_PLACE_SEARCH_DATE_KEY)
                .orEmpty()

            EditPlaceSearchScreen(
                dateKey = dateKey,
                onBackClick = {
                    onPlaceEditSearchCancelled()
                    navController.popBackStack()
                },
                onPlaceSelectedForEdit = { place ->
                    onPlaceEditSearchResult(place)
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
