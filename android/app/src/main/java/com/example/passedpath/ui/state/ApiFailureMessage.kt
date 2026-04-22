package com.example.passedpath.ui.state

object ApiFailureMessage {
    const val NETWORK_REQUEST_FAILED: String =
        "네트워크 요청에 실패했습니다. 잠시 후 다시 시도해 주세요."

    fun fromThrowable(throwable: Throwable): String {
        return NETWORK_REQUEST_FAILED
    }
}
