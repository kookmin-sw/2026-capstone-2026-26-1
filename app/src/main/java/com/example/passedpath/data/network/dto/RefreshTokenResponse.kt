package com.example.passedpath.data.network.dto

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)