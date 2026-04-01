package backend.capstone.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KakaoLocalConfig {

    @Value("${kakao.local.url}")
    private String kakaoLocalUrl;

    @Value("${kakao.local.rest-api-key}")
    private String kakaoRestApiKey;

    @Bean
    public WebClient kakaoLocalWebClient(
        WebClient.Builder builder
    ) {
        return builder
            .baseUrl(kakaoLocalUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey)
            .build();
    }

}
