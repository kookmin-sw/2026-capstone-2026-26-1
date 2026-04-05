package com.example.passedpath.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.passedpath.feature.auth.presentation.screen.LoginRoute
import com.example.passedpath.feature.auth.presentation.state.AuthEvent
import com.example.passedpath.feature.friends.presentation.screen.FriendsRoute
import com.example.passedpath.feature.main.presentation.screen.MainRoute
import com.example.passedpath.feature.mypage.presentation.screen.MyPageRoute
import com.example.passedpath.feature.permission.presentation.screen.LocationPermissionIntroRoute
import com.example.passedpath.ui.component.toast.MessageToast

@Composable
fun AppNavHost(
    navController: NavHostController,
    appEntryViewModel: AppEntryViewModel
) {
    var logoutToastMessage by remember { mutableStateOf<String?>(null) }
    var logoutToastTrigger by remember { mutableStateOf(0) }

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
        AppScaffold(navController = navController) { modifier: Modifier ->
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
                    MainRoute()
                }

                composable(NavRoute.MYPAGE) {
                    MyPageRoute()
                }
            }
        }

        logoutToastMessage?.let { message ->
            MessageToast(
                message = message,
                triggerKey = "logout:$logoutToastTrigger:$message",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 28.dp)
            )
        }
    }
}
