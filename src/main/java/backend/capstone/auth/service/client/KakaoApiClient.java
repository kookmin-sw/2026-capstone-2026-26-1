package backend.capstone.auth.service.client;

import backend.capstone.auth.service.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
				status -> status.is4xxClientError(),
				resp -> resp.bodyToMono(String.class)
					.flatMap(body -> Mono.error(new RuntimeException("Kakao API 4xx: " + body)))
				//TODO: 커스텀 예외로 변경
			)
			.onStatus(
				status -> status.is4xxClientError(),
				resp -> resp.bodyToMono(String.class)
					.flatMap(body -> Mono.error(new RuntimeException("Kakao API 5xx: " + body)))
				//TODO: 커스텀 예외로 변경
			)
			.bodyToMono(KakaoUserInfoResponse.class)
			.block(); //TODO: block이랑 reactive랑 뭔차이임
	}


}
