package backend.capstone.domain.kakaoplace.service;

import backend.capstone.domain.kakaoplace.service.dto.KakaoSearchByCoordResult;
import backend.capstone.domain.kakaoplace.dto.SearchResultByCategoryAndCoord;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class KakaoSearchByCoordService {

    private final WebClient kakaoLocalWebClient;

    /**
     * 적절한 POI가 없을 때 중심좌표 기반 주소 fallback
     */
    public Optional<SearchResultByCategoryAndCoord> searchByCoord(double latitude, double longitude) {
        KakaoSearchByCoordResult response = kakaoLocalWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/geo/coord2address.json")
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("input_coord", "WGS84")
                .build())
            .retrieve()
            .bodyToMono(KakaoSearchByCoordResult.class)
            .block();

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            return Optional.empty();
        }

        KakaoSearchByCoordResult.Document doc = response.documents().getFirst();

        String placeName = extractPlaceName(doc);
        String roadAddress = extractRoadAddress(doc);
        String jibunAddress = extractJibunAddress(doc);

        Double resolvedLatitude = extractAddressLatitude(doc);
        Double resolvedLongitude = extractAddressLongitude(doc);

        return Optional.of(
            SearchResultByCategoryAndCoord.builder()
                .name(placeName)
                .roadAddress(roadAddress)
                .jibunAddress(jibunAddress)
                .latitude(resolvedLatitude != null ? resolvedLatitude : latitude)
                .longitude(resolvedLongitude != null ? resolvedLongitude : longitude)
                .build()
        );
    }

    private String extractPlaceName(KakaoSearchByCoordResult.Document document) {
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

        return null;
    }

    private String extractRoadAddress(KakaoSearchByCoordResult.Document document) {
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

    private String extractJibunAddress(KakaoSearchByCoordResult.Document document) {
        if (document != null && document.address() != null) {
            String addressName = document.address().address_name();
            if (addressName != null && !addressName.isBlank()) {
                return addressName;
            }
        }
        return null;
    }

    private Double extractAddressLatitude(KakaoSearchByCoordResult.Document document) {
        if (document == null) {
            return null;
        }

        if (document.road_address() != null) {
            Double y = parseDouble(document.road_address().y());
            if (y != null) {
                return y;
            }
        }

        if (document.address() != null) {
            return parseDouble(document.address().y());
        }

        return null;
    }

    private Double extractAddressLongitude(KakaoSearchByCoordResult.Document document) {
        if (document == null) {
            return null;
        }

        if (document.road_address() != null) {
            Double x = parseDouble(document.road_address().x());
            if (x != null) {
                return x;
            }
        }

        if (document.address() != null) {
            return parseDouble(document.address().x());
        }

        return null;
    }

    private Double parseDouble(String value) {
        try {
            return value == null || value.isBlank() ? null : Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

}
