package backend.capstone.domain.place.service;

import backend.capstone.domain.place.dto.PlaceSearchResponse;
import backend.capstone.domain.place.exception.PlaceErrorCode;
import backend.capstone.domain.place.mapper.PlaceMapper;
import backend.capstone.domain.place.service.dto.NaverLocalSearchResult;
import backend.capstone.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Service
@RequiredArgsConstructor
public class NaverLocalSearchService {

    private final WebClient naverLocalWebClient;

    @Transactional(readOnly = true)
    public PlaceSearchResponse searchPlaces(String query) {
        try {
            NaverLocalSearchResult result = naverLocalWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v1/search/local.json")
                    .queryParam("query", query)
                    .queryParam("display", 5)
                    .queryParam("start", 1)
                    .queryParam("sort", "random")
                    .build())
                .retrieve()
                .bodyToMono(NaverLocalSearchResult.class)
                .block();

            if (result == null) {
                throw new BusinessException(PlaceErrorCode.NAVER_PLACE_SEARCH_FAILED);
            }

            return PlaceMapper.toPlaceSearchResponse(result);
        } catch (WebClientException e) {
            throw new BusinessException(PlaceErrorCode.NAVER_PLACE_SEARCH_FAILED);
        }
    }
}
