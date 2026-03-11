package com.example.passedpath.feature.auth.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.passedpath.R
import com.example.passedpath.feature.auth.presentation.viewmodel.LoginViewModel
import com.example.passedpath.feature.permission.data.manager.LocationPermissionGate
import com.example.passedpath.navigation.NavRoute
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(180.dp))

        Image(
            painter = painterResource(id = R.drawable.login_logo),
            contentDescription = "logo for login screen",
            modifier = Modifier
                .fillMaxHeight(0.1f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "지나온 길을 \n기록합니다",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.kakaoLogin(
                    context = context,
                    onLoginSuccess = {
                        val nextRoute = if (LocationPermissionGate.isBackgroundAlwaysGranted(context)) {
                            NavRoute.MAIN
                        } else {
                            NavRoute.PERMISSION_INTRO
                        }

                        navController.navigate(nextRoute) {
                            popUpTo(NavRoute.LOGIN) { inclusive = true }
                        }
                    },
                    onLoginError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEE500),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Login with Kakao",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    PassedPathTheme {
        LoginScreen(
            navController = rememberNavController()
        )
    }
}
