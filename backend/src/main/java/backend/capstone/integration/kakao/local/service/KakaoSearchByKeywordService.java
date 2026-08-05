package backend.capstone.integration.kakao.local.service;

import backend.capstone.global.exception.BusinessException;
import backend.capstone.integration.kakao.local.client.KakaoLocalApiClient;
import backend.capstone.integration.kakao.local.dto.KakaoSearchByKeywordResult;
import backend.capstone.integration.kakao.local.dto.PlaceSearchResponse;
import backend.capstone.integration.kakao.local.exception.KakaoPlaceErrorCode;
import backend.capstone.integration.kakao.local.mapper.KakaoPlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

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
        } catch (RestClientException e) {
            throw new BusinessException(KakaoPlaceErrorCode.KAKAO_PLACE_SEARCH_FAILED);
        }
    }
}
