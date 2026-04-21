package com.example.passedpath.feature.auth.data.remote.dto

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
