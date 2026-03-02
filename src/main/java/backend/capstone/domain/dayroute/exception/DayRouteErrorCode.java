package backend.capstone.domain.dayroute.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DayRouteErrorCode implements ErrorCode {

    CANNOT_ACCESS_DAY_ROUTE("해당 경로에 접근할 수 없습니다.", HttpStatus.UNAUTHORIZED),
    DAY_ROUTE_CREATE_FAILED("일차 경로 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    GPS_POINT_UPLOAD_FAILURE("좌표 업로드에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;
}
