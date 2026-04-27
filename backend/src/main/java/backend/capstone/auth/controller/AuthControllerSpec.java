package backend.capstone.auth.controller;

import backend.capstone.auth.dto.KakaoLoginRequest;
import backend.capstone.auth.dto.LoginResponse;
import backend.capstone.auth.dto.TokenPair;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "인증 API")
public interface AuthControllerSpec {

    @Operation(
        summary = "카카오 로그인",
        description = """
            카카오 엑세스 토큰을 받아서 카카오 유저를 조회하고 db에 존재하는 유저면 로그인, 존재하지 않는 유저면 회원가입 후 로그인합니다.<br>
            응답으로 받은 dayStartTime은 지나온길을 초기화하는 시작 시간이고, dayEndTime은 종료시간입니다.
            """
    )
    LoginResponse kakaoLogin(KakaoLoginRequest request);

    @Operation(
        summary = "토큰 재발급",
        description = """
            API 응답으로 401 상태코드와 함께 다음과 같은 엑세스 토큰 만료 메시지를 받을 때 이 API를 호출합니다.
            ```json
            {
              "code": "ACCESS_TOKEN_EXPIRED",
              "message": "만료된 액세스 토큰입니다."
            }
            ```
            X-Refresh-Token 헤더에 리프레시 토큰을 넣고 Authorization 헤더에 만료된 엑세스 토큰을 넣어주세요.
            이 API의 응답으로 받은 엑세스 토큰과 리프레시 토큰을 사용해주세요. (그전에 저장한 엑세스 토큰과 리프레시 토큰은 폐지)
            """
    )
    TokenPair refresh(HttpServletRequest request, String refreshToken);

    @Operation(
        summary = "테스트용 토큰 발급",
        description = """
            테스트 유저에 대한 엑세스 토큰과 리프레시 토큰을 발급합니다.
            리프레시 토큰을 활용한 토큰 재발급 또한 가능합니다.
            테스트용으로만 사용해주세요. (나중에 이 API는 지울 예정)
            """
    )
    TokenPair issueTestJwt();

    @Operation(
        summary = "혜원이 계정 전용 토큰 발급",
        description = """
            혜원이 카카오 계정에 전용 엑세스 토큰과 리프레시 토큰을 발급합니다.<br>
            리프레시 토큰을 활용한 토큰 재발급 또한 가능합니다.
            """
    )
    TokenPair issueHyewonJwt();
}
