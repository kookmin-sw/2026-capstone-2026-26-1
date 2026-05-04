package backend.capstone.domain.user.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

	NOT_FOUND_USER("해당 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

	private final String message;
	private final HttpStatus status;

}
