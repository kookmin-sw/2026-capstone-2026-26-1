package backend.capstone.auth.jwt.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum JwtErrorCode implements ErrorCode {
	EXPIRED("만료된 엑세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
	INVALID_SIGNATURE("유효하지 않은 서명 형식입니다.", HttpStatus.UNAUTHORIZED),
	MALFORMED("구조가 깨진 엑세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
	UNSUPPORTED("지원하지 않는 형식입니다.", HttpStatus.UNAUTHORIZED),
	INVALID_TOKEN("유효하지 않은 엑세스 토큰입니다.", HttpStatus.UNAUTHORIZED);

	private final String message;
	private final HttpStatus status;
}
