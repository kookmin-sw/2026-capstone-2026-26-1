package com.example.passedpath.data.network.dto

// DTO(data transfer object)
// 서버로 request하는 데이터의 형태 정의
data class KakaoLoginRequest(
    val kakaoAccessToken: String
)