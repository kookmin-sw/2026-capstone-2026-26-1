package com.example.passedpath.feature.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.passedpath.feature.auth.LoginViewModel
import com.example.passedpath.navigation.NavRoute

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Companion.Start
    ) {

        Spacer(modifier = Modifier.Companion.height(40.dp))

        Box(
            modifier = Modifier.Companion
                .size(48.dp)
                .background(Color.Companion.LightGray)
        )

        Spacer(modifier = Modifier.Companion.height(24.dp))

        Text(
            text = "하루를 따라\n기록합니다",
            fontSize = 28.sp,
            fontWeight = FontWeight.Companion.Bold,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.Companion.height(40.dp))

        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.Companion.LightGray)
        )

        Spacer(modifier = Modifier.Companion.weight(1f))

        Button(
            onClick = {
                viewModel.kakaoLogin(
                    context = context,
                    onLoginSuccess = {
                        // 로그인 성공 → 메인 화면 이동
                        navController.navigate(NavRoute.PERMISSION_INTRO) {
                            popUpTo(NavRoute.LOGIN) { inclusive = true }
                        }
                    },
                    onLoginError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.Companion
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEE500),
                contentColor = Color.Companion.Black
            )
        ) {
            Text(
                text = "카카오 로그인",
                fontSize = 16.sp,
                fontWeight = FontWeight.Companion.Bold
            )
        }

    }
}