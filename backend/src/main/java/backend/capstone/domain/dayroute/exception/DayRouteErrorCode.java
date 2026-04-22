package backend.capstone.domain.dayroute.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DayRouteErrorCode implements ErrorCode {

    DAY_ROUTE_NOT_FOUND("해당 일차의 경로의 데이터가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    DAY_ROUTE_CREATE_FAILED("일차 경로 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    GPS_POINT_UPLOAD_FAILURE("좌표 업로드에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;
}
