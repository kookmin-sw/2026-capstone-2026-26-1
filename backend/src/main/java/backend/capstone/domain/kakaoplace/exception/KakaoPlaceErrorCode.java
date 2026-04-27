package backend.capstone.domain.kakaoplace.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum KakaoPlaceErrorCode implements ErrorCode {

    KAKAO_PLACE_SEARCH_FAILED("카카오 장소 검색 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY);

    private final String message;
    private final HttpStatus status;

}
