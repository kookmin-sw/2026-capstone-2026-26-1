package backend.capstone.auth.dto;

public record LoginResponse(
    Long userId,
    String nickname,
    String accessToken,
    String refreshToken
) {

}
