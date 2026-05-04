package backend.capstone.domain.kakaoplace.service;

import backend.capstone.domain.kakaoplace.exception.KakaoPlaceErrorCode;
import backend.capstone.domain.kakaoplace.dto.PlaceSearchResponse;
import backend.capstone.domain.kakaoplace.mapper.KakaoPlaceMapper;
import backend.capstone.domain.kakaoplace.service.client.KakaoLocalApiClient;
import backend.capstone.domain.kakaoplace.service.dto.KakaoSearchByKeywordResult;
import backend.capstone.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientException;

@Service
@RequiredArgsConstructor
public class KakaoSearchByKeywordService {

    private final KakaoLocalApiClient kakaoLocalApiClient;

    @Transactional(readOnly = true)
    public PlaceSearchResponse searchByKeyword(String query, int page) {
        try {
            KakaoSearchByKeywordResult result = kakaoLocalApiClient.searchByKeyword(query, page);

            if (result == null) {
                throw new BusinessException(KakaoPlaceErrorCode.KAKAO_PLACE_SEARCH_FAILED);
            }

            return KakaoPlaceMapper.toPlaceSearchResponse(page, result);
        } catch (WebClientException e) {
            throw new BusinessException(KakaoPlaceErrorCode.KAKAO_PLACE_SEARCH_FAILED);
        }
    }
}
