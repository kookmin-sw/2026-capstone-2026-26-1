package com.example.passedpath.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.passedpath.data.datastore.TokenDataStore
import com.example.passedpath.feature.auth.presentation.AuthEvent
import com.example.passedpath.feature.auth.presentation.LoginScreen
import com.example.passedpath.feature.main.MainScreen
import com.example.passedpath.feature.permission.LocationPermissionGate
import com.example.passedpath.feature.permission.LocationPermissionIntroScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    /* =========================================================
       🔐 1. 로그아웃 플로우
       - 어디서든 로그아웃 이벤트 발생 시
       - 토큰 제거 후 LOGIN으로 강제 이동
       ========================================================= */
    LaunchedEffect(Unit) {
        AuthEvent.logoutEvent.collect {
            navController.navigate(NavRoute.LOGIN) {
                popUpTo(0)
            }
        }
    }

    /* =========================================================
       🚀 2. 앱 시작 진입 플로우 (Entry Routing)
       1) AccessToken 존재 여부 확인
       2) 존재하면 ALWAYS 권한 상태 확인
       3) 최종 시작 목적지 결정
       ========================================================= */
    val startDestination = remember {
        runBlocking {

            val token = TokenDataStore.getAccessToken(context)

            // 🔹 로그인 안된 상태
            if (token == null) {
                return@runBlocking NavRoute.LOGIN
            }

            // 🔹 로그인 완료 상태 → ALWAYS 권한 확인
            val alwaysGranted =
                LocationPermissionGate.isBackgroundAlwaysGranted(context)

            if (alwaysGranted) {
                NavRoute.MAIN
            } else {
                NavRoute.PERMISSION_INTRO
            }
        }
    }

    /* =========================================================
       🧭 3. NavHost 구성
       - 큰 단계(로그인 / 권한 안내 / 메인)만 담당
       - 기능 제한은 Main 내부에서 처리
       ========================================================= */
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        /* ==============================
           🔐 로그인 플로우
           Login → 성공 시 Intro or Main 이동
           ============================== */
        composable(NavRoute.LOGIN) {
            LoginScreen(navController = navController)
        }

        /* ==============================
           📍 위치 권한 안내 플로우
           Intro → 권한 요청 → 설정 이동 → ALWAYS 확인
           성공 시 Main 이동
           ============================== */
        composable(NavRoute.PERMISSION_INTRO) {
            LocationPermissionIntroScreen(
                onPermissionResolved = {
                    navController.navigate(NavRoute.MAIN) {
                        popUpTo(NavRoute.PERMISSION_INTRO) { inclusive = true }
                    }
                }
            )
        }

        /* ==============================
           🏠 메인 플로우
           - ALWAYS 권한 여부와 관계없이 진입
           - 기능 단위에서 제한 처리
           ============================== */
        composable(NavRoute.MAIN) {
            MainScreen(
                onLogout = {
                    coroutineScope.launch {

                        // 🔹 토큰 제거
                        TokenDataStore.clear(context)

                        // 🔹 로그인 화면으로 이동
                        navController.navigate(NavRoute.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            )
        }
    }
}
