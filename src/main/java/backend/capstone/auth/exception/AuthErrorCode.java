package backend.capstone.auth.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_KAKAO_ACCESS_TOKEN("유효하지 않은 카카오 엑세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
    KAKAO_SERVER_ERROR("카카오 서버 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    HASH_NOT_SUPPORT("해시 알고리즘을 지원하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCESS_TOKEN_EXPIRED("만료된 액세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN("유효하지 않은 액세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
    MISSING_ACCESS_TOKEN("액세스 토큰이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다. 다시 로그인해주세요", HttpStatus.UNAUTHORIZED),
    UNKNOWN_ERROR("알 수 없는 오류가 발생했습니다.", HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus status;
}
