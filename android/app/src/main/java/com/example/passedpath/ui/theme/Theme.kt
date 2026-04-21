package com.example.passedpath.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// -------------------------------
// ColorScheme 정의
// -------------------------------

// 라이트 테마에서 사용할, 역할별 색상 매핑
// 실제 Color 값은 Color.kt에 정의해 둠
private val LightColorScheme = lightColorScheme(
    primary = Primary,          // 메인 CTA
    onPrimary = Color.White,    // 메인 CTA의 내용물

    secondary = Green50,       // 보조 CTA
    onSecondary = Primary,

    outline = Green100,          // 테두리, 경계선

    background = Color.White,   // 앱 최하단 바탕의 배경색
    surface = Color.White,      // 바탕 위에 올라가는 시트(팝업, 모달 등) 배경색
    onSurface = Color.Black,
)

// 다크 테마에서 사용할, 역할별 색상 매핕
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)


// -------------------------------
// 사용자 정의 Theme 함수
// -------------------------------

@Composable
fun PassedPathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    // dynamicColorTheme : 사용자가 커스텀한 디바이스 테마와 비슷하게 생성된 시스템테마
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {

        // dynamicColorTheme은 안드로이드12 부터 제공하는 가능
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        // default는 라이트 테마
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}