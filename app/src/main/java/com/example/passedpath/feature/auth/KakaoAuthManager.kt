package com.example.passedpath.feature.auth

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

object KakaoAuthManager {

    fun login(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (Throwable?) -> Unit
    ) {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                token != null -> onSuccess(token.accessToken)
                else -> onError(error)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(
                context = context,
                callback = callback   // ✅ 이거 중요
            )
        } else {
            UserApiClient.instance.loginWithKakaoAccount(
                context = context,
                callback = callback   // ✅ 이거 중요
            )
        }
    }
}