package com.example.passedpath.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.passedpath.R

// Set of Material typography styles to start with

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),  // 400
    Font(R.font.pretendard_medium, FontWeight.Medium),          // 500
    Font(R.font.pretendard_bold, FontWeight.Bold),              // 700
    Font(R.font.pretendard_semibold, FontWeight.SemiBold)      // 600

)

// 텍스트 스타일 정의
val Typography = Typography(

    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),

    labelLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

