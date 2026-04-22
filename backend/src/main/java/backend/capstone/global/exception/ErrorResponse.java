package backend.capstone.global.exception;

import java.util.List;
import org.springframework.validation.FieldError;

public record ErrorResponse(
    String code,
    String message,
    List<FieldErrorDetail> fieldErrors
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            fieldErrors
        );
    }

    public record FieldErrorDetail(
        String field,
        String reason
    ) {

        public FieldErrorDetail(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }

        public static FieldErrorDetail of(String field, String reason) {
            return new FieldErrorDetail(field, reason);
        }
    }

}
