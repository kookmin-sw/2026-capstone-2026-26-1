package backend.capstone.global.exception;

import backend.capstone.global.exception.ErrorResponse.FieldErrorDetail;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 비지니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("BusinessException 발생: {}", e.getErrorCode());

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    // 2. 입력값 검증
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();

        //타입 변환 실패: 예) 날짜 형식 틀림, 숫자 자리에 문자열
        if (cause instanceof InvalidFormatException ex) {
            String fieldName = extractLastFieldName(ex.getPath());

            Object rejectedValue = ex.getValue();

            return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT_FORMAT.getStatus())
                .body(ErrorResponse.of(
                        CommonErrorCode.INVALID_INPUT_FORMAT,
                        List.of(
                            FieldErrorDetail.of(
                                fieldName,
                                String.format("'%s' 값의 형식이 올바르지 않습니다.", rejectedValue)
                            ))
                    )
                );
        }

        // JSON 구조 자체가 DTO와 안 맞음
        if (cause instanceof MismatchedInputException ex) {
            String fieldName = extractLastFieldName(ex.getPath());

            return ResponseEntity.status(CommonErrorCode.INVALID_JSON_STRUCTURE.getStatus())
                .body(ErrorResponse.of(CommonErrorCode.INVALID_INPUT_FORMAT,
                        List.of(
                            FieldErrorDetail.of(
                                fieldName,
                                "입력값 구조가 올바르지 않습니다."
                            ))
                    )
                );
        }

        // json 문법 에러 등
        return ResponseEntity.status(CommonErrorCode.MALFORMED_JSON.getStatus())
            .body(ErrorResponse.of(CommonErrorCode.MALFORMED_JSON));
    }

    private String extractLastFieldName(List<Reference> ex) {
        String fieldName = ex.stream()
            .map(ref -> ref.getFieldName())
            .reduce((first, second) -> second)
            .orElse("unknown");
        return fieldName;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException e) {
        List<FieldErrorDetail> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldErrorDetail::new)
            .toList();

        return ResponseEntity.status(CommonErrorCode.VALIDATION_DTO_ERROR.getStatus())
            .body(ErrorResponse.of(CommonErrorCode.VALIDATION_DTO_ERROR, fieldErrors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String field = e.getName(); // 파라미터 이름
        String message = String.format("'%s' 값의 형식이 올바르지 않습니다.", field);

        return ResponseEntity.status(CommonErrorCode.INVALID_TYPE.getStatus())
            .body(ErrorResponse.of(CommonErrorCode.INVALID_TYPE,
                List.of(FieldErrorDetail.of(field, message))
            ));
    }

    // 메서드 파라미터 검증 실패(@RequestParam, @PathVariable)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        List<FieldErrorDetail> fieldErrors = e.getConstraintViolations()
            .stream()
            .map(v ->
                FieldErrorDetail.of(
                    v.getPropertyPath().toString(),
                    v.getMessage()
                ))
            .toList();

        return ResponseEntity.status(CommonErrorCode.VALIDATION_REQUEST_PARAMETER_ERROR.getStatus())
            .body(
                ErrorResponse.of(CommonErrorCode.VALIDATION_REQUEST_PARAMETER_ERROR, fieldErrors));
    }

    // 3. 서버 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception 발생", e);

        return ResponseEntity
            .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}
