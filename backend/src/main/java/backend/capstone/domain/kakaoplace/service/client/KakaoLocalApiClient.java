package backend.capstone.domain.kakaoplace.service.client;

import backend.capstone.domain.kakaoplace.service.dto.KakaoSearchByCategoryResult;
import backend.capstone.domain.kakaoplace.service.dto.KakaoSearchByCoordResult;
import backend.capstone.domain.kakaoplace.service.dto.KakaoSearchByKeywordResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KakaoLocalApiClient {

    private static final int DEFAULT_KEYWORD_PAGE_SIZE = 10;

    private final WebClient kakaoLocalWebClient;

    public KakaoLocalApiClient(@Qualifier("kakaoLocalWebClient") WebClient kakaoLocalWebClient) {
        this.kakaoLocalWebClient = kakaoLocalWebClient;
    }

    public KakaoSearchByKeywordResult searchByKeyword(String query, int page) {
        return kakaoLocalWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("size", DEFAULT_KEYWORD_PAGE_SIZE)
                .build())
            .retrieve()
            .bodyToMono(KakaoSearchByKeywordResult.class)
            .block();
    }

    public KakaoSearchByCoordResult searchByCoord(double latitude, double longitude) {
        return kakaoLocalWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/geo/coord2address.json")
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("input_coord", "WGS84")
                .build())
            .retrieve()
            .bodyToMono(KakaoSearchByCoordResult.class)
            .block();
    }

    public KakaoSearchByCategoryResult searchByCategory(String categoryGroupCode, double latitude,
        double longitude, int radius, int size
    ) {
        return kakaoLocalWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/search/category.json")
                .queryParam("category_group_code", categoryGroupCode)
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("radius", radius)
                .queryParam("sort", "distance")
                .queryParam("size", size)
                .build())
            .retrieve()
            .bodyToMono(KakaoSearchByCategoryResult.class)
            .block();
    }
}
