package com.example.passedpath.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.auth.presentation.screen.LoginScreen
import com.example.passedpath.feature.auth.presentation.state.AuthEvent
import com.example.passedpath.feature.main.presentation.screen.MainScreen
import com.example.passedpath.feature.permission.presentation.screen.LocationPermissionIntroScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appContainer = context.appContainer

    LaunchedEffect(Unit) {
        AuthEvent.logoutEvent.collect {
            navController.navigate(NavRoute.LOGIN) {
                popUpTo(0)
            }
        }
    }

    val startDestination = remember {
        runBlocking {
            val token = appContainer.authSessionStorage.getAccessToken()

            if (token == null) {
                return@runBlocking NavRoute.LOGIN
            }

            val alwaysGranted = appContainer.permissionChecker.isBackgroundAlwaysGranted()

            if (alwaysGranted) {
                NavRoute.MAIN
            } else {
                NavRoute.PERMISSION_INTRO
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoute.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(NavRoute.PERMISSION_INTRO) {
            LocationPermissionIntroScreen(
                onPermissionResolved = {
                    navController.navigate(NavRoute.MAIN) {
                        popUpTo(NavRoute.PERMISSION_INTRO) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.MAIN) {
            MainScreen(
                onLogout = {
                    coroutineScope.launch {
                        appContainer.authSessionStorage.clear()

                        navController.navigate(NavRoute.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            )
        }
    }
}
