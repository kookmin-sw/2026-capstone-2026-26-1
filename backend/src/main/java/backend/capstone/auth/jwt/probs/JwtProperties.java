package backend.capstone.auth.jwt.probs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	String secret,
	long accessExpSeconds,
	long refreshExpSeconds
) {

}
