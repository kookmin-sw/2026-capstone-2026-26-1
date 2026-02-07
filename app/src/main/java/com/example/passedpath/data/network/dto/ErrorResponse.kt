package com.example.passedpath.data.network.dto


// 공통 에러 응답 (서버는 auth용 에러응답으로 만들어두긴함 프론트는 일단 공통에러로 사용할것임)
data class ErrorResponse(
    val code: String,
    val message: String
)

