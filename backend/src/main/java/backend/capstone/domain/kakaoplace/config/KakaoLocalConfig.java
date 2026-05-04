package backend.capstone.domain.kakaoplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KakaoLocalConfig {

    @Value("${kakao.local.base-url}")
    private String baseUrl;

    @Value("${kakao.local.rest-api-key}")
    private String restApiKey;

    @Bean("kakaoLocalWebClient")
    public WebClient kakaoLocalWebClient(
        @Qualifier("webClientBuilder") WebClient.Builder builder
    ) {
        return builder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
            .build();
    }

}
