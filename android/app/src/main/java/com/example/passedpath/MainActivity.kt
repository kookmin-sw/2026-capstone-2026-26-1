package com.example.passedpath

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.care.presentation.deeplink.CareInviteDeepLinkParser
import com.example.passedpath.navigation.AppEntryState
import com.example.passedpath.navigation.AppEntryViewModel
import com.example.passedpath.navigation.AppEntryViewModelFactory
import com.example.passedpath.navigation.AppNavHost
import com.example.passedpath.ui.theme.PassedPathTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val appEntryViewModel: AppEntryViewModel by viewModels {
        AppEntryViewModelFactory(applicationContext.appContainer)
    }
    private val pendingCareInviteCode = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            appEntryViewModel.state.value is AppEntryState.Loading
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        super.onCreate(savedInstanceState)
        publishCareInviteCode(intent)

        setContent {
            PassedPathTheme {
                val navController = rememberNavController()
                val careInviteCode = pendingCareInviteCode.collectAsStateWithLifecycle().value

                AppNavHost(
                    navController = navController,
                    appEntryViewModel = appEntryViewModel,
                    pendingCareInviteCode = careInviteCode,
                    onCareInviteCodeConsumed = { consumedCode ->
                        if (pendingCareInviteCode.value == consumedCode) {
                            pendingCareInviteCode.value = null
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        publishCareInviteCode(intent)
    }

    private fun publishCareInviteCode(intent: Intent?) {
        val inviteCode = CareInviteDeepLinkParser.extractInviteCode(intent?.data) ?: return
        pendingCareInviteCode.value = inviteCode
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
