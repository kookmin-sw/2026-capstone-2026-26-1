package backend.capstone.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NaverLocalConfig {

    @Value("${naver.local.base-url}")
    private String baseUrl;

    @Value("${naver.local.client-id}")
    private String clientId;

    @Value("${naver.local.client-secret}")
    private String clientSecret;

    @Bean
    public WebClient naverLocalWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(baseUrl)
            .defaultHeader("X-Naver-Client-Id", clientId)
            .defaultHeader("X-Naver-Client-Secret", clientSecret)
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .build();
    }
}
