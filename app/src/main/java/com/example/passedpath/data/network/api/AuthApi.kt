package com.example.passedpath.data.network.api

import com.example.passedpath.data.network.dto.KakaoLoginRequest
import com.example.passedpath.data.network.dto.KakaoLoginResponse
import com.example.passedpath.data.network.dto.RefreshTokenResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// API interface: retrofit이 http 요청을 함수 호출처럼 할 수 있도록 도와주는 인터페이스
// 함수 호출을 http 요청으로 변환하는 역할

// Retrofit flow
// 1. AutoApi.loginWithKaKao() 호출
// 2. Retrofit이 HTTP Request message 생성
// 3. OkHttp가 네트워크 통신
// 4. 서버에서 응답 - 데이터 형태는 JSON
// 5. json을 DTO 객체에 매핑해서 kotlin으로 변환
// 6. Call <KakaoLoginResponse>
interface AuthApi {

    @POST("/api/auth/login/kakao")
    suspend fun loginWithKakao(
        // request message의 body에 넣을 데이터
        @Body request: KakaoLoginRequest
    ): KakaoLoginResponse

    @POST("/api/auth/refresh")
    suspend fun refreshToken(
        @Header("X-Refresh-Token") refreshToken: String
    ): RefreshTokenResponse
}