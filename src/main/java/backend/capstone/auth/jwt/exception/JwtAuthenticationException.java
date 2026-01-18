package backend.capstone.auth.jwt.exception;

import backend.capstone.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class JwtAuthenticationException extends RuntimeException {

	private final ErrorCode errorCode;

	public JwtAuthenticationException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

}
