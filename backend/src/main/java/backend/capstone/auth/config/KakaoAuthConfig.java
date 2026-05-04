package backend.capstone.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KakaoAuthConfig {

    @Value("${kakao.auth.base-url}")
    private String baseUrl;

    @Bean("kakaoAuthWebClient")
    public WebClient kakaoAuthWebClient(
        @Qualifier("webClientBuilder") WebClient.Builder builder
    ) {
        return builder
            .baseUrl(baseUrl)
            .build();
    }
}
