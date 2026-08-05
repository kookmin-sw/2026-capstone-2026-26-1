package backend.capstone.integration.kakao.local.client;

import backend.capstone.integration.kakao.local.dto.KakaoSearchByCategoryResult;
import backend.capstone.integration.kakao.local.dto.KakaoSearchByCoordResult;
import backend.capstone.integration.kakao.local.dto.KakaoSearchByKeywordResult;
import backend.capstone.integration.kakao.local.dto.KakaoSearchByRegionCodeResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoLocalApiClient {

    private static final int DEFAULT_KEYWORD_PAGE_SIZE = 10;

    private final RestClient kakaoLocalRestClient;

    public KakaoLocalApiClient(
        @Qualifier("kakaoLocalRestClient") RestClient kakaoLocalRestClient) {
        this.kakaoLocalRestClient = kakaoLocalRestClient;
    }

    public KakaoSearchByKeywordResult searchByKeyword(String query, int page) {
        return kakaoLocalRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("size", DEFAULT_KEYWORD_PAGE_SIZE)
                .build())
            .retrieve()
            .body(KakaoSearchByKeywordResult.class);
    }

    public KakaoSearchByCoordResult searchByCoord(double latitude, double longitude) {
        return kakaoLocalRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/geo/coord2address.json")
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("input_coord", "WGS84")
                .build())
            .retrieve()
            .body(KakaoSearchByCoordResult.class);
    }

    public KakaoSearchByRegionCodeResult searchRegionCodeByCoord(double latitude,
        double longitude) {
        return kakaoLocalRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/geo/coord2regioncode.json")
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("input_coord", "WGS84")
                .build())
            .retrieve()
            .body(KakaoSearchByRegionCodeResult.class);
    }

    public KakaoSearchByCategoryResult searchByCategory(String categoryGroupCode, double latitude,
        double longitude, int radius, int size
    ) {
        return kakaoLocalRestClient.get()
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
            .body(KakaoSearchByCategoryResult.class);
    }
}
