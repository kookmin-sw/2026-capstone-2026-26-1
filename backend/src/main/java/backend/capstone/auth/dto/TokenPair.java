package backend.capstone.auth.dto;

public record TokenPair(
    String accessToken,
    String refreshToken) {

}
