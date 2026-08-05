package backend.capstone.integration.kakao.auth.client;

import backend.capstone.auth.exception.AuthErrorCode;
import backend.capstone.global.exception.BusinessException;
import backend.capstone.integration.kakao.auth.dto.KakaoUserInfoResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoAuthApiClient {

    private static final String USER_INFO_URI = "/v2/user/me";

    private final RestClient kakaoAuthRestClient;

    public KakaoAuthApiClient(@Qualifier("kakaoAuthRestClient") RestClient kakaoAuthRestClient) {
        this.kakaoAuthRestClient = kakaoAuthRestClient;
    }

    public KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        return kakaoAuthRestClient.get()
            .uri(USER_INFO_URI)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
            .retrieve()
            .onStatus(
                HttpStatusCode::is4xxClientError,
                (request, response) -> {
                    throw new BusinessException(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN);
                }
            )
            .onStatus(
                HttpStatusCode::is5xxServerError,
                (request, response) -> {
                    throw new BusinessException(AuthErrorCode.KAKAO_SERVER_ERROR);
                }
            )
            .body(KakaoUserInfoResponse.class);
    }
}
