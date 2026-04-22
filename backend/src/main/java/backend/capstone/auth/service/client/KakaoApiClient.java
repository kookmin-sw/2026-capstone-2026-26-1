package backend.capstone.auth.service.client;

import backend.capstone.auth.exception.AuthErrorCode;
import backend.capstone.auth.service.dto.KakaoUserInfoResponse;
import backend.capstone.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KakaoApiClient {

	private final WebClient webClient;
	private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

	public KakaoUserInfoResponse getUserInfo(String kakaoAccessToken) {
		return webClient.get()
			.uri(USER_INFO_URI)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
			.retrieve()
			// 카카오 토큰이 만료/위조면 여기서 401/403 등이 떨어짐
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
			.block(); //TODO: block이랑 reactive랑 뭔차이임
	}


}
