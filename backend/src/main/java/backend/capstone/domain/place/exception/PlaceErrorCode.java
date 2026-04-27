package backend.capstone.domain.place.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PlaceErrorCode implements ErrorCode {

    PLACE_NOT_FOUND("장소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PLACE_REORDER("잘못된 장소 순서 변경 요청입니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;
}
