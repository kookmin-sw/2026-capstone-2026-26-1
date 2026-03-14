package com.example.passedpath.feature.auth.presentation.screen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.auth.presentation.viewmodel.LoginDestination
import com.example.passedpath.feature.auth.presentation.viewmodel.LoginViewModel
import com.example.passedpath.feature.auth.presentation.viewmodel.LoginViewModelFactory
import com.example.passedpath.navigation.NavRoute

@Composable
fun LoginRoute(
    onNavigate: (String) -> Unit,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current

    LoginScreen(
        onLoginClick = {
            viewModel.kakaoLogin(
                context = context,
                onLoginSuccess = { destination ->
                    val nextRoute = when (destination) {
                        LoginDestination.MAIN -> NavRoute.MAIN
                        LoginDestination.PERMISSION_INTRO -> NavRoute.PERMISSION_INTRO
                    }

                    onNavigate(nextRoute)
                },
                onLoginError = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    )
}
