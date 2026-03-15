package com.example.passedpath.feature.mypage.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.mypage.presentation.viewmodel.MyPageViewModel
import com.example.passedpath.feature.mypage.presentation.viewmodel.MyPageViewModelFactory

@Composable
fun MyPageRoute(
    viewModel: MyPageViewModel = viewModel(
        factory = MyPageViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val testResult by viewModel.testResult.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    MyPageScreen(
        userProfile = userProfile,
        testResult = testResult,
        onTestClick = viewModel::testApi,
        onLogoutClick = viewModel::logout
    )
}
