package backend.capstone.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

	// TODO: validate에서 “왜 실패했는지” 로깅/에러 분기 만료인지, 위조인지 구분하면 디버깅 쉬움
	public boolean validate(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Long getUserIdFromToken(String token) {
		Claims claims = parseClaims(token);
		return Long.parseLong(claims.getSubject());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith((javax.crypto.SecretKey) key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

}
