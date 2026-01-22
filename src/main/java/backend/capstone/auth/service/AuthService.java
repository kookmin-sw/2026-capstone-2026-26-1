package backend.capstone.auth.service;

import backend.capstone.auth.dto.LoginResponse;
import backend.capstone.auth.jwt.probs.JwtProperties;
import backend.capstone.auth.jwt.service.JwtTokenProvider;
import backend.capstone.auth.service.client.KakaoApiClient;
import backend.capstone.auth.service.dto.KakaoUserInfoResponse;
import backend.capstone.auth.util.RefreshTokenHasher;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoApiClient kakaoApiClient;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties props;

    @Transactional
    public LoginResponse kakaoLogin(String kakaoAccessToken) {
        KakaoUserInfoResponse kakaoUser = kakaoApiClient.getUserInfo(kakaoAccessToken);
        User user = userService.upsertKakaoUser(kakaoUser);
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        return new LoginResponse(user.getId(), user.getNickname(), user.getProfileImageUrl(),
            accessToken, refreshToken);
    }

    private String redisKey(Long userId) {
        return "rt:user:" + userId;
    }

    public void save(Long userId, String refreshTokenRaw) {
        String hash = RefreshTokenHasher.sha256Hex(refreshTokenRaw);
        redisTemplate.opsForValue().set(redisKey(userId), hash, props.refreshExpSeconds());
    }

    public boolean validateRefreshToken(Long userId, String refreshTokenRaw) {
        String storedHash = redisTemplate.opsForValue().get(redisKey(userId));
        if (storedHash == null) {
            return false;
        }
        String incomingHash = RefreshTokenHasher.sha256Hex(refreshTokenRaw);
        return storedHash.equals(incomingHash);
    }

    public void delete(Long userId) {
        redisTemplate.delete(redisKey(userId));
    }


}
