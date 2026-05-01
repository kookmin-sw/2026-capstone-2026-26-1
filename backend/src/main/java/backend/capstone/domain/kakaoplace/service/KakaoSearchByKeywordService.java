package backend.capstone.domain.kakaoplace.service;

import backend.capstone.domain.kakaoplace.exception.KakaoPlaceErrorCode;
import backend.capstone.domain.kakaoplace.dto.PlaceSearchResponse;
import backend.capstone.domain.kakaoplace.mapper.KakaoPlaceMapper;
import backend.capstone.domain.kakaoplace.service.dto.KakaoSearchByKeywordResult;
import backend.capstone.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Service
@RequiredArgsConstructor
public class KakaoSearchByKeywordService {

    private final @Qualifier("kakaoLocalWebClient") WebClient kakaoLocalWebClient;

    @Transactional(readOnly = true)
    public PlaceSearchResponse searchByKeyword(String query, int page) {
        try {
            KakaoSearchByKeywordResult result = kakaoLocalWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .queryParam("page", page)
                    .queryParam("size", 10)
                    .build())
                .retrieve()
                .bodyToMono(KakaoSearchByKeywordResult.class)
                .block();

            if (result == null) {
                throw new BusinessException(KakaoPlaceErrorCode.KAKAO_PLACE_SEARCH_FAILED);
            }

            return KakaoPlaceMapper.toPlaceSearchResponse(page, result);
        } catch (WebClientException e) {
            throw new BusinessException(KakaoPlaceErrorCode.KAKAO_PLACE_SEARCH_FAILED);
        }
    }
}
