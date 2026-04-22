package backend.capstone.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    default String getCode() {
        return ((Enum<?>) this).name();
    }

    String getMessage();

    HttpStatus getStatus();
}
