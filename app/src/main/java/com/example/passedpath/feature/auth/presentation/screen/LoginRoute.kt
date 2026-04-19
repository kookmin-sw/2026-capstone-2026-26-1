package com.example.passedpath.feature.auth.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.auth.presentation.viewmodel.LoginDestination
import com.example.passedpath.feature.auth.presentation.viewmodel.LoginEffect
import com.example.passedpath.feature.auth.presentation.viewmodel.LoginViewModel
import com.example.passedpath.feature.auth.presentation.viewmodel.LoginViewModelFactory
import com.example.passedpath.navigation.NavRoute

@Composable
fun LoginRoute(
    onNavigate: (String) -> Unit,
    onShowToastMessage: (String) -> Unit,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.Navigate -> {
                    val nextRoute = when (effect.destination) {
                        LoginDestination.MAIN -> NavRoute.MAIN
                        LoginDestination.PERMISSION_INTRO -> NavRoute.PERMISSION_INTRO
                    }
                    onNavigate(nextRoute)
                }

                is LoginEffect.ShowError -> {
                    onShowToastMessage(context.getString(effect.messageResId))
                }
            }
        }
    }

    LoginScreen(
        onLoginClick = {
            viewModel.kakaoLogin(context = context)
        }
    )
}
