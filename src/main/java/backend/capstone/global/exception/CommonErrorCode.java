package backend.capstone.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // 400
    INVALID_INPUT_FORMAT(HttpStatus.BAD_REQUEST, "입력값 형식이 올바르지 않습니다."),
    INVALID_JSON_STRUCTURE(HttpStatus.BAD_REQUEST, "요청 JSON 구조가 올바르지 않습니다."),
    MALFORMED_JSON(HttpStatus.BAD_REQUEST, "요청 JSON 문법이 올바르지 않습니다."),
    VALIDATION_DTO_ERROR(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
    VALIDATION_PATH_VARIABLE_ERROR(HttpStatus.BAD_REQUEST, "경로 변수 혹은 쿼리 파라미터 검증에 실패했습니다."),
    INVALID_TYPE(HttpStatus.BAD_REQUEST, "입력값 타입이 올바르지 않습니다"),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
