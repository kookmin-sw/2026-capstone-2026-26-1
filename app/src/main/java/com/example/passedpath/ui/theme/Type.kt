package com.example.passedpath.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.passedpath.R


val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),  // 400
    Font(R.font.pretendard_medium, FontWeight.Medium),          // 500
    Font(R.font.pretendard_bold, FontWeight.Bold),              // 700
    Font(R.font.pretendard_semibold, FontWeight.SemiBold)      // 600
)

// 텍스트 스타일 정의
val Typography = Typography(

    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 41.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 33.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontSize = 14.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Medium
    ),
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontSize = 12.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    ),

    labelLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    )
)

