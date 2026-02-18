package backend.capstone.domain.dayroute.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DayRouteErrorCode implements ErrorCode {

    CANNOT_ACCESS_DAY_ROUTE("해당 경로에 접근할 수 없습니다.", HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus status;
}
