package backend.capstone.domain.place.service;

import backend.capstone.domain.place.exception.PlaceErrorCode;
import backend.capstone.domain.place.service.dto.KakaoTransCoordResponse;
import backend.capstone.domain.place.service.dto.KakaoTransCoordResponse.Document;
import backend.capstone.domain.place.service.dto.Wgs84Coordinate;
import backend.capstone.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Service
@RequiredArgsConstructor
public class KakaoCoordinateConversionService {

    private final WebClient kakaoLocalWebClient;

    public Wgs84Coordinate convertTm128ToWgs84(double tm128X, double tm128Y) {
        try {
            KakaoTransCoordResponse response = kakaoLocalWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/local/geo/transcoord.json")
                    .queryParam("x", tm128X)
                    .queryParam("y", tm128Y)
                    .queryParam("input_coord", "KTM")
                    .queryParam("output_coord", "WGS84")
                    .build())
                .retrieve()
                .bodyToMono(KakaoTransCoordResponse.class)
                .block();

            if (response == null || response.documents() == null
                || response.documents().isEmpty()) {
                throw new BusinessException(PlaceErrorCode.KAKAO_COORDINATE_CONVERSION_FAILED);
            }

            Document document = response.documents().getFirst();
            if (document.x() == null || document.y() == null) {
                throw new BusinessException(PlaceErrorCode.KAKAO_COORDINATE_CONVERSION_FAILED);
            }

            return new Wgs84Coordinate(document.y(), document.x());
        } catch (WebClientException e) {
            throw new BusinessException(PlaceErrorCode.KAKAO_COORDINATE_CONVERSION_FAILED);
        }
    }
}
