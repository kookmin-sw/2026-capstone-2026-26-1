package backend.capstone.auth.service;

import backend.capstone.auth.dto.LoginResponse;
import backend.capstone.auth.dto.TokenPair;
import backend.capstone.auth.exception.AuthErrorCode;
import backend.capstone.auth.jwt.service.JwtTokenProvider;
import backend.capstone.auth.service.client.KakaoApiClient;
import backend.capstone.auth.service.dto.KakaoUserInfoResponse;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import backend.capstone.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoApiClient kakaoApiClient;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse kakaoLogin(String kakaoAccessToken) {
        KakaoUserInfoResponse kakaoUser = kakaoApiClient.getUserInfo(kakaoAccessToken);
        User user = userService.upsertKakaoUser(kakaoUser);
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenService.save(user.getId(), refreshToken);

        return new LoginResponse(user.getId(), user.getNickname(), user.getProfileImageUrl(),
            accessToken, refreshToken);
    }

    @Transactional
    public TokenPair refreshAccessToken(String refreshToken) {
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        String newAccess = jwtTokenProvider.createAccessToken(userId);
        String newRefresh = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenService.save(userId, newRefresh); //유저당 1개면 덮어쓰기

        return new TokenPair(newAccess, newRefresh);
    }

    @Transactional
    public TokenPair testIssue() {
        String refreshToken = jwtTokenProvider.createRefreshToken(1L);
        refreshTokenService.save(1L, refreshToken);
        return new TokenPair(jwtTokenProvider.createAccessToken(1L),
            refreshToken);
    }


}
