package backend.capstone.integration.kakao.local.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class KakaoLocalConfig {

    @Value("${kakao.local.base-url}")
    private String baseUrl;

    @Value("${kakao.local.rest-api-key}")
    private String restApiKey;

    @Bean("kakaoLocalRestClient")
    public RestClient kakaoLocalRestClient(
        @Qualifier("restClientBuilder") RestClient.Builder builder
    ) {
        return builder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
            .build();
    }

}
