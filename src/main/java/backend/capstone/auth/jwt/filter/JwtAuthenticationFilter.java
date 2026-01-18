package backend.capstone.auth.jwt.filter;

import backend.capstone.auth.jwt.exception.JwtAuthenticationException;
import backend.capstone.auth.jwt.service.JwtTokenProvider;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;
	private final UserService userService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String token = resolveBearerToken(request);

		try {
			tokenProvider.validateOrThrow(token);

			Long userId = tokenProvider.getUserIdFromToken(token);
			User user = userService.findById(userId);

			UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(user, null, List.of());

			SecurityContextHolder.getContext().setAuthentication(auth);

		} catch (JwtAuthenticationException e) {
			SecurityContextHolder.clearContext();

			// TODO: 로깅: 운영에선 token 전문 찍지 말고 앞부분만/해시만
			log.info("[JWT] rejected: code={}, msg={}, uri={}, token={}",
				e.getErrorCode(), e.getMessage(), request.getRequestURI(), token);
		}

		filterChain.doFilter(request, response);

	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri.startsWith("/swagger-ui")
			|| uri.equals("/swagger-ui.html")
			|| uri.startsWith("/v3/api-docs")
			|| uri.startsWith("/webjars")
			|| uri.startsWith("/swagger-resources")
			|| uri.startsWith("/api/auth")
			|| uri.startsWith("/oauth2")
			|| uri.startsWith("/login")
			|| uri.equals("/favicon.ico");
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header == null) {
			return null;
		}
		if (!header.startsWith("Bearer ")) {
			return null;
		}
		return header.substring(7);
	}
}
