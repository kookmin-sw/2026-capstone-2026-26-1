package backend.capstone.integration.kakao.auth.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class KakaoAuthConfig {

    @Value("${kakao.auth.base-url}")
    private String baseUrl;

    @Bean("kakaoAuthRestClient")
    public RestClient kakaoAuthRestClient(
        @Qualifier("restClientBuilder") RestClient.Builder builder
    ) {
        return builder
            .baseUrl(baseUrl)
            .build();
    }
}
