package com.example.passedpath.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.passedpath.data.datastore.TokenDataStore
import com.example.passedpath.feature.auth.AuthEvent
import com.example.passedpath.feature.auth.LoginScreen
import com.example.passedpath.feature.main.MainScreen
import com.example.passedpath.feature.permission.LocationPermissionGateScreen
import com.example.passedpath.feature.permission.LocationPermissionIntroScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@Composable
fun AppNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 로그아웃 이벤트 수신
    LaunchedEffect(Unit) {
        AuthEvent.logoutEvent.collect {
            Log.d("AUTH", "강제 로그아웃 처리")

            navController.navigate(NavRoute.LOGIN) {
                popUpTo(0)
            }
        }
    }

    // 앱 시작 시 accessToken 존재 여부 확인
    val startDestination = remember {
        runBlocking {
            val token = TokenDataStore.getAccessToken(context)
            if (token == null) {
                Log.d("AUTH", "저장된 accessToken 없음 → Login 시작")
                return@runBlocking NavRoute.LOGIN
            }

            val alwaysGranted = com.example.passedpath.feature.permission.LocationPermissionGate
                .isBackgroundAlwaysGranted(context)

            if (alwaysGranted) {
                Log.d("AUTH", "토큰 있음 + ALWAYS 허용 → Main 시작")
                NavRoute.MAIN
            } else {
                Log.d("AUTH", "토큰 있음 + ALWAYS 미허용 → PermissionIntro 시작")
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
                onStartClick = {
                    navController.navigate(NavRoute.PERMISSION_GATE)
                }
            )
        }

        composable(NavRoute.PERMISSION_GATE) {
            LocationPermissionGateScreen(
                navController = navController,
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            )
        }

        composable(NavRoute.MAIN) {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            MainScreen(
                onLogout = {
                    coroutineScope.launch {

                        // accessToken 삭제
                        TokenDataStore.clear(context)
                        Log.d("AUTH", "accessToken 삭제")

                        // 로그인 화면으로 이동
                        navController.navigate(NavRoute.LOGIN) {
                            popUpTo(NavRoute.MAIN) { inclusive = true }
                            Log.d("AUTH", "Login 화면 이동")
                        }
                    }
                }
            )
        }

    }
}