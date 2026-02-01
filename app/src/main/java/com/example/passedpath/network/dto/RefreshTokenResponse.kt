package com.example.passedpath.network.dto

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)