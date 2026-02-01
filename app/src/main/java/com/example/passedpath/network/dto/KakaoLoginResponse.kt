package com.example.passedpath.network.dto

// DTO(Data Transfer Object)
// 서버로부터 전달받는 데이터 객체 정의
// 정확히는, HTTP Body에 들어갈 JSON의 구조
// 이를 통해 JSON <-> Kotlin 매핑


// request -> json을 kotlin으로 변환해서 전달
// response -> json 데이터를 받아서 kotlin으로 변환해서 저장


data class KakaoLoginResponse(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String,
    val accessToken: String,
    val refreshToken: String
)