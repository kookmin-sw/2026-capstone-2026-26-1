package com.example.passedpath.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    appEntryViewModel: AppEntryViewModel
) {
    LaunchedEffect(Unit) {
        AuthEvent.logoutEvent.collect {
            navController.navigate(NavRoute.LOGIN) {
                popUpTo(0)
            }
        }
    }

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
}
