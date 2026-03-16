package backend.capstone.auth.service;

import backend.capstone.auth.dto.LoginResponse;
import backend.capstone.auth.dto.TokenPair;
import backend.capstone.auth.exception.AuthErrorCode;
import backend.capstone.auth.jwt.TokenStatus;
import backend.capstone.auth.jwt.service.JwtTokenProvider;
import backend.capstone.auth.service.client.KakaoApiClient;
import backend.capstone.auth.service.dto.KakaoUserInfoResponse;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import backend.capstone.global.exception.BusinessException;
import io.jsonwebtoken.Claims;
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
    private final JwtTokenProvider tokenProvider;

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
    public TokenPair refreshAccessToken(String accessToken, String refreshToken) {
        Long refreshUserId = validateRefreshToken(refreshToken);
        Long accessUserId = validateAccessToken(accessToken);

        if (!refreshUserId.equals(accessUserId)) {
            throw new BusinessException(AuthErrorCode.TOKEN_USER_MISMATCH);
        }

        String newAccess = jwtTokenProvider.createAccessToken(refreshUserId);
        String newRefresh = jwtTokenProvider.createRefreshToken(refreshUserId);

        refreshTokenService.save(refreshUserId, newRefresh); //유저당 1개면 덮어쓰기

        return new TokenPair(newAccess, newRefresh);
    }

    @Transactional
    public TokenPair testIssue() {
        String refreshToken = jwtTokenProvider.createRefreshToken(1L);
        refreshTokenService.save(1L, refreshToken);
        return new TokenPair(jwtTokenProvider.createAccessToken(1L),
            refreshToken);
    }

    private Long validateRefreshToken(String refreshToken) {
        TokenStatus tokenStatus = tokenProvider.validateToken(refreshToken);

        if (tokenStatus != TokenStatus.VALID) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        return userId;
    }

    private Long validateAccessToken(String accessToken) {
        Claims claims = tokenProvider.parseClaimsAllowExpired(accessToken);
        String tokenType = claims.get("token_type", String.class);
        if (!tokenType.equals("ACCESS")) {
            throw new BusinessException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }
        return Long.parseLong(claims.getSubject());
    }


}
