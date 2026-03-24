package backend.capstone.global.exception;

public record ErrorResponse(
    String code,
    String message
) {

}
