package backend.capstone.domain.place.service;

import backend.capstone.domain.place.service.dto.KakaoCategorySearchResponse;
import backend.capstone.domain.place.service.dto.KakaoCategorySearchResponse.Document;
import backend.capstone.domain.place.service.dto.KakaoCoord2AddressResponse;
import backend.capstone.domain.place.service.dto.PlaceSearchResult;
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
public class PlaceSearchService {

    private static final int DEFAULT_RADIUS_METER = 100;
    private static final int DEFAULT_SIZE = 15;

    //TODO: 카테고리 가중치 고려
    private static final List<String> ALL_CATEGORY_GROUP_CODES = List.of(
        "MT1", // 대형마트
        "CS2", // 편의점
        "PS3", // 어린이집, 유치원
        "SC4", // 학교
        "AC5", // 학원
        "PK6", // 주차장
        "OL7", // 주유소, 충전소
        "SW8", // 지하철역
        "BK9", // 은행
        "CT1", // 문화시설
        "AG2", // 중개업소
        "PO3", // 공공기관
        "AT4", // 관광명소
        "AD5", // 숙박
        "FD6", // 음식점
        "CE7", // 카페
        "HP8", // 병원
        "PM9"  // 약국
    );

    private final WebClient kakaoLocalWebClient;

    public Optional<PlaceSearchResult> searchByCoordinate(double latitude, double longitude) {
        Optional<Document> bestPoi = findBestPoi(latitude, longitude);
        if (bestPoi.isEmpty()) {
            return Optional.empty();
        }

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

    private Optional<KakaoCategorySearchResponse.Document> findBestPoi(
        double latitude,
        double longitude
    ) {
        Map<String, Document> uniqueCandidates = new LinkedHashMap<>();

        for (String categoryGroupCode : ALL_CATEGORY_GROUP_CODES) {
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
            .min(Comparator.comparingInt(this::distanceOnlyScore));

    }

    /**
     * 최적 후보 선정 기준: 오직 거리
     */
    private int distanceOnlyScore(KakaoCategorySearchResponse.Document doc) {
        Integer distance = parseInteger(doc.distance()); //distance는 좌표와 장소 간의 직선거리(m)
        return distance != null ? distance : Integer.MAX_VALUE;
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

    //TODO: 건물명, 도로명 주소, 지번주소 등등 여러 후보 중에서 어떤걸 장소명으로 쓸지 기준 세우기
    //TODO: 건물명이 없다면 가까운 건물명이라도 어떻게 조회해올 수 없나
    private String extractPlaceName(KakaoCoord2AddressResponse.Document document) {
        if (document.road_address() != null) {
            String buildingName = document.road_address().building_name();
            if (buildingName != null && !buildingName.isBlank()) {
                return buildingName;
            }

            String roadAddressName = document.road_address().address_name();
            if (roadAddressName != null && !roadAddressName.isBlank()) {
                return roadAddressName;
            }
        }

        if (document.address() != null) {
            String addressName = document.address().address_name();
            if (addressName != null && !addressName.isBlank()) {
                return addressName;
            }
        }

        return "알 수 없는 장소";
    }

    private String extractRoadAddress(KakaoCoord2AddressResponse.Document document) {
        if (document.road_address() != null) {
            String roadAddressName = document.road_address().address_name();
            if (roadAddressName != null && !roadAddressName.isBlank()) {
                return roadAddressName;
            }
        }

        if (document.address() != null) {
            return document.address().address_name();
        }

        return null;
    }
}
