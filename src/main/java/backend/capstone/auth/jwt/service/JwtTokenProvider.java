package backend.capstone.auth.jwt.service;

import backend.capstone.auth.exception.AuthErrorCode;
import backend.capstone.auth.jwt.TokenStatus;
import backend.capstone.auth.jwt.TokenType;
import backend.capstone.auth.jwt.probs.JwtProperties;
import backend.capstone.global.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties props;
    private final Key key;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("token_type", TokenType.ACCESS.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(props.accessExpSeconds())))
            .signWith(key)
            .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("token_type", TokenType.REFRESH.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(props.refreshExpSeconds())))
            .signWith(key)
            .compact();
    }

    public TokenStatus validateToken(String token) {
        if (token == null || token.isBlank()) {
            return TokenStatus.MISSING_TOKEN;
        }

        try {
            parseClaims(token);
            return TokenStatus.VALID;
        } catch (ExpiredJwtException e) {
            return TokenStatus.EXPIRED;
        } catch (UnsupportedJwtException e) {
            return TokenStatus.UNSUPPORTED;
        } catch (MalformedJwtException e) {
            return TokenStatus.MALFORMED;
        } catch (SecurityException | SignatureException e) {
            // 위조/서명불일치/secret 불일치
            return TokenStatus.INVALID_SIGNATURE;
        } catch (IllegalArgumentException e) {
            return TokenStatus.INVALID_TOKEN;
        } catch (JwtException e) {
            // 기타 JWT 관련 예외
            return TokenStatus.INVALID_TOKEN;
        }
    }

    public Long getUserIdFromAccessToken(String token) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get("token_type", String.class);
        if (!TokenType.ACCESS.name().equals(tokenType)) {
            throw new BusinessException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }
        return Long.parseLong(claims.getSubject());
    }

    public Long getUserIdFromRefreshToken(String token) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get("token_type", String.class);
        if (!TokenType.REFRESH.name().equals(tokenType)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        return Long.parseLong(claims.getSubject());
    }


    //토큰 파싱
    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith((SecretKey) key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Claims parseClaimsAllowExpired(String token) {
        try {
            return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

}
