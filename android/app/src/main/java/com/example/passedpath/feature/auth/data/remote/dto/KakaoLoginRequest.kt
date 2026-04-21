package com.example.passedpath.feature.auth.data.remote.dto

// DTO(data transfer object)
// 서버로 request하는 데이터의 형태 정의
data class KakaoLoginRequest(
    val kakaoAccessToken: String
)
