package backend.capstone.auth.service.dto;

public record KakaoUserInfoResponse(
    String id,
    KakaoAccount kakao_account
) {

    public record KakaoAccount(
        Profile profile
    ) {

    }

    public record Profile(
        String nickname
    ) {

    }

}
