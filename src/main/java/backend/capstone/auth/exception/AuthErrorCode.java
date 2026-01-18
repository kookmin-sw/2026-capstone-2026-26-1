package backend.capstone.auth.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	INVALID_KAKAO_ACCESS_TOKEN("유효하지 않은 카카오 엑세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
	KAKAO_SERVER_ERROR("카카오 서버 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

	private final String message;
	private final HttpStatus status;
}
