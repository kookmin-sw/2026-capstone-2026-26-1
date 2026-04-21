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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.passedpath.feature.auth.presentation.screen.LoginRoute
import com.example.passedpath.feature.auth.presentation.state.AuthEvent
import com.example.passedpath.feature.friends.presentation.screen.FriendsRoute
import com.example.passedpath.feature.main.presentation.screen.MainRoute
import com.example.passedpath.feature.mypage.presentation.screen.MyPageRoute
import com.example.passedpath.feature.permission.presentation.screen.LocationPermissionIntroRoute
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
        AppScaffold(
            navController = navController,
            onBottomBarReselected = { route ->
                if (route == NavRoute.MAIN) {
                    mainTabReselectionEvent++
                }
            }
        ) { modifier: Modifier ->
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
                        onShowToastMessage = { message ->
                            loginToastMessage = message
                            loginToastTrigger++
                        }
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
                    FriendsRoute()
                }

                composable(NavRoute.MAIN) {
                    MainRoute(mainTabReselectionEvent = mainTabReselectionEvent)
                }

                composable(NavRoute.MYPAGE) {
                    MyPageRoute()
                }
            }
        }

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
