package com.example.passedpath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.passedpath.app.appContainer
import com.example.passedpath.navigation.AppEntryState
import com.example.passedpath.navigation.AppEntryViewModel
import com.example.passedpath.navigation.AppEntryViewModelFactory
import com.example.passedpath.navigation.AppNavHost
import com.example.passedpath.ui.theme.PassedPathTheme

class MainActivity : ComponentActivity() {
    private val appEntryViewModel: AppEntryViewModel by viewModels {
        AppEntryViewModelFactory(applicationContext.appContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            appEntryViewModel.state.value is AppEntryState.Loading
        }

        super.onCreate(savedInstanceState)

        setContent {
            PassedPathTheme {
                val navController = rememberNavController()

                AppNavHost(
                    navController = navController,
                    appEntryViewModel = appEntryViewModel
                )
            }
        }
    }
}
