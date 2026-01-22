package backend.capstone.auth.service;

import backend.capstone.auth.jwt.probs.JwtProperties;
import backend.capstone.auth.util.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties props;

    private String redisKey(Long userId) {
        return "rt:user:" + userId;
    }

    public boolean validateRefreshToken(Long userId, String refreshTokenRaw) {
        String storedHash = redisTemplate.opsForValue().get(redisKey(userId));
        if (storedHash == null) {
            return false;
        }
        String incomingHash = RefreshTokenHasher.sha256Hex(refreshTokenRaw);
        return storedHash.equals(incomingHash);
    }

    public void save(Long userId, String refreshTokenRaw) {
        String hash = RefreshTokenHasher.sha256Hex(refreshTokenRaw);
        redisTemplate.opsForValue().set(redisKey(userId), hash, props.refreshExpSeconds());
    }

    public void delete(Long userId) {
        redisTemplate.delete(redisKey(userId));
    }
}
