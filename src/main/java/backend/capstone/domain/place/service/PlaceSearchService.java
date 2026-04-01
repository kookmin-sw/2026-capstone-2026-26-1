package backend.capstone.domain.place.service;

import backend.capstone.domain.place.service.dto.KakaoCoord2AddressResponse;
import backend.capstone.domain.place.service.dto.PlaceSearchResult;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final WebClient kakaoLocalWebClient;

    public Optional<PlaceSearchResult> searchByCoordinate(double latitude, double longitude) {
        KakaoCoord2AddressResponse response = kakaoLocalWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("input_coord", "WGS84") //WGS84좌표계 사용
                .build())
            .retrieve()
            .bodyToMono(KakaoCoord2AddressResponse.class)
            .block();

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            return Optional.empty();
        }

        KakaoCoord2AddressResponse.Document document = response.documents()
            .getFirst(); //여러 후보중 맨앞의 결과만 추출

        String placeName = extractPlaceName(document);
        String roadAddress = extractRoadAddress(document);

        return Optional.of(
            PlaceSearchResult.builder()
                .name(placeName)
                .roadAddress(roadAddress)
                .latitude(latitude)
                .longitude(longitude)
                .build()
        );
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

    //TODO: 우선ㄴ순위는 도로명 주소->지번주소->NULL
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
