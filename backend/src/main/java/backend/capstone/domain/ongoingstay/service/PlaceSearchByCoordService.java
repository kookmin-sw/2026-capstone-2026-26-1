package backend.capstone.domain.ongoingstay.service;

import backend.capstone.domain.ongoingstay.service.dto.KakaoCategorySearchResponse;
import backend.capstone.domain.ongoingstay.service.dto.KakaoCategorySearchResponse.Document;
import backend.capstone.domain.ongoingstay.service.dto.PlaceSearchResult;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class PlaceSearchByCoordService {

    private static final int DEFAULT_RADIUS_METER = 100;
    private static final int DEFAULT_SIZE = 15;

    /**
     * 1차 탐색 카테고리 앱 취지상 "의미있는 장소"일 가능성이 높은 카테고리
     */
    private static final List<String> PRIMARY_CATEGORY_GROUP_CODES = List.of(
        "SC4", // 학교
        "AC5", // 학원
        "CT1", // 문화시설
        "AT4", // 관광명소
        "AD5", // 숙박
        "FD6", // 음식점
        "CE7", // 카페
        "HP8" // 병원
    );

    /**
     * 2차 확장 카테고리 1차 탐색에서 아무 후보도 없을 때만 추가 탐색
     */
    private static final List<String> SECONDARY_CATEGORY_GROUP_CODES = List.of(
        "MT1", // 대형마트
        "CS2", // 편의점
        "BK9", // 은행
        "PO3", // 공공기관
        "PM9",  // 약국
        "OL7",  // 주유소, 충전소
        "SW8" //지하철역
    );

    private final WebClient kakaoLocalWebClient;
    private final PlaceSearchFallbackService placeSearchFallbackService;

    public Optional<PlaceSearchResult> searchByCoordinate(double latitude, double longitude) {
        Optional<Document> bestPoi = findBestPoi(latitude, longitude);
        if (bestPoi.isPresent()) {
            Document doc = bestPoi.get();
            return Optional.of(
                PlaceSearchResult.builder()
                    .name(emptyToNull(doc.place_name()))
                    .roadAddress(emptyToNull(doc.road_address_name()))
                    .jibunAddress(emptyToNull(doc.address_name()))
                    .latitude(parseDouble(doc.y()))
                    .longitude(parseDouble(doc.x()))
                    .build()
            );
        }

        return placeSearchFallbackService.searchAddressFallback(latitude, longitude);
    }

    private Optional<KakaoCategorySearchResponse.Document> findBestPoi(
        double latitude,
        double longitude
    ) {
        // 1차 카테고리부터 우선 탐색
        Optional<KakaoCategorySearchResponse.Document> primaryBestPoi =
            findBestPoiByCategories(latitude, longitude, PRIMARY_CATEGORY_GROUP_CODES);

        if (primaryBestPoi.isPresent()) {
            return primaryBestPoi;
        }

        // 1차에서 후보가 없을 때만 2차 카테고리 탐색
        return findBestPoiByCategories(latitude, longitude, SECONDARY_CATEGORY_GROUP_CODES);
    }

    //거리+카테고리 기반으로 최적의 poi를 반환하는 함수
    private Optional<KakaoCategorySearchResponse.Document> findBestPoiByCategories(
        double latitude,
        double longitude,
        List<String> categoryGroupCodes
    ) {
        Map<String, Document> uniqueCandidates = new LinkedHashMap<>();

        for (String categoryGroupCode : categoryGroupCodes) {
            KakaoCategorySearchResponse response = kakaoLocalWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/local/search/category.json")
                    .queryParam("category_group_code", categoryGroupCode)
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .queryParam("radius", DEFAULT_RADIUS_METER)
                    .queryParam("sort", "distance")
                    .queryParam("size", DEFAULT_SIZE)
                    .build())
                .retrieve()
                .bodyToMono(KakaoCategorySearchResponse.class)
                .block();

            if (response == null || response.documents() == null || response.documents()
                .isEmpty()) {
                continue;
            }

            for (Document doc : response.documents()) {
                if (emptyToNull(doc.place_name()) == null) {
                    continue;
                }

                // 같은 장소가 여러 카테고리 탐색 과정에서 중복 수집될 수 있으므로 id 기준 dedupe
                uniqueCandidates.putIfAbsent(doc.id(), doc);
            }
        }
        if (uniqueCandidates.isEmpty()) {
            return Optional.empty();
        }

        return uniqueCandidates.values().stream()
            .min(Comparator.comparingInt(this::score));
    }

    /**
     * 최적 후보 선정 기준: 거리+카테고리별 가중치
     */
    private int score(KakaoCategorySearchResponse.Document doc) {
        Integer distance = parseInteger(doc.distance()); //distance는 좌표와 장소 간의 직선거리(m)
        if (distance == null) {
            return Integer.MAX_VALUE;
        }

        int score = distance;
        String categoryGroupCode = emptyToNull(doc.category_group_code());
        score += categoryWeight(categoryGroupCode);

        return score;
    }

    private int categoryWeight(String categoryGroupCode) {
        if (categoryGroupCode == null) {
            return 0;
        }

        return switch (categoryGroupCode) {
            case "SC4" -> -18; // 학교 (최우선)
            case "AC5" -> -16; // 학원
            case "CT1" -> -10; // 문화시설
            case "AT4" -> -10; // 관광명소
            case "AD5" -> -8;  // 숙박
            case "FD6" -> -7;  // 음식점
            case "CE7" -> -7;  // 카페
            case "HP8" -> -5;  // 병원
            default -> 0;
        };
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            return value == null || value.isBlank() ? null : Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }


}
