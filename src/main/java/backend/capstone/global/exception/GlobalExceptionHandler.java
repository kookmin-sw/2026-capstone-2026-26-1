package backend.capstone.global.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("BusinessException 발생: {}", e.getErrorCode());

        ErrorCode errorCode = e.getErrorCode();

        String code = (errorCode instanceof Enum)
            ? ((Enum<?>) errorCode).name()
            : errorCode.getClass().getSimpleName();

        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(new ErrorResponse(code, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();

        //타입 변환 실패: 예) 날짜 형식 틀림, 숫자 자리에 문자열
        if (cause instanceof InvalidFormatException ex) {
            String fieldName = ex.getPath().stream()
                .map(ref -> ref.getFieldName())
                .reduce((first, second) -> second)
                .orElse("unknown");

            String message = String.format(
                "'%s' 필드의 값 '%s' 이(가) 올바른 형식이 아닙니다. 요청 형식을 확인해주세요.",
                fieldName,
                ex.getValue()
            );

            return ResponseEntity.badRequest().body(
                new ErrorResponse("BAD_REQUEST", message)
            );
        }

        // JSON 구조 자체가 DTO와 안 맞음
        if (cause instanceof MismatchedInputException ex) {
            String fieldName = ex.getPath().stream()
                .map(ref -> ref.getFieldName())
                .reduce((first, second) -> second)
                .orElse("unknown");

            String message = String.format(
                "'%s' 필드의 입력값 구조가 올바르지 않습니다.",
                fieldName
            );

            return ResponseEntity.badRequest().body(
                new ErrorResponse("BAD_REQUEST", message)
            );
        }

        // json 문법 에러 등
        return ResponseEntity.badRequest().body(
            new ErrorResponse("BAD_REQUEST", "요청 JSON 형식이 올바르지 않습니다.")
        );
    }


}
