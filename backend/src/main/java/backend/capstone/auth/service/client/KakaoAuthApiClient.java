package backend.capstone.auth.service.client;

import backend.capstone.auth.exception.AuthErrorCode;
import backend.capstone.auth.service.dto.KakaoUserInfoResponse;
import backend.capstone.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class KakaoAuthApiClient {

    private static final String USER_INFO_URI = "/v2/user/me";

    private final WebClient kakaoAuthWebClient;

    public KakaoAuthApiClient(@Qualifier("kakaoAuthWebClient") WebClient kakaoAuthWebClient) {
        this.kakaoAuthWebClient = kakaoAuthWebClient;
    }

    public KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
        return kakaoAuthWebClient.get()
            .uri(USER_INFO_URI)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
            .retrieve()
            .onStatus(
                HttpStatusCode::is4xxClientError,
                resp -> resp.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(
                        new BusinessException(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN)))
            )
            .onStatus(
                HttpStatusCode::is5xxServerError,
                resp -> resp.bodyToMono(String.class)
                    .flatMap(
                        body -> Mono.error(new BusinessException(AuthErrorCode.KAKAO_SERVER_ERROR)))
            )
            .bodyToMono(KakaoUserInfoResponse.class)
            .block();
    }
}
