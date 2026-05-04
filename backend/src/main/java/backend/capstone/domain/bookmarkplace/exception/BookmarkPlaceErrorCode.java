package backend.capstone.domain.bookmarkplace.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BookmarkPlaceErrorCode implements ErrorCode {

    BOOKMARK_PLACE_NOT_FOUND("즐겨찾기 장소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    HOME_BOOKMARK_PLACE_ALREADY_EXISTS("HOME 타입 즐겨찾기 장소는 1개만 등록할 수 있습니다.", HttpStatus.CONFLICT);

    private final String message;
    private final HttpStatus status;
}
