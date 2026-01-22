package backend.capstone.auth.jwt.service;

import backend.capstone.auth.jwt.exception.JwtAuthenticationException;
import backend.capstone.auth.jwt.exception.JwtErrorCode;
import backend.capstone.auth.jwt.probs.JwtProperties;
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
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(props.accessExpSeconds())))
            .signWith(key)
            .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(props.refreshExpSeconds())))
            .signWith(key)
            .compact();
    }

    public boolean validateOrThrow(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.EXPIRED);

        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.UNSUPPORTED);

        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.MALFORMED);

        } catch (SecurityException | SignatureException e) {
            // 위조/서명불일치/secret 불일치
            throw new JwtAuthenticationException(JwtErrorCode.INVALID_SIGNATURE);

        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException(JwtErrorCode.INVALID_TOKEN);

        } catch (JwtException e) {
            // 기타 JWT 관련 예외
            throw new JwtAuthenticationException(JwtErrorCode.INVALID_TOKEN);
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    //토큰 파싱
    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith((javax.crypto.SecretKey) key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

}
