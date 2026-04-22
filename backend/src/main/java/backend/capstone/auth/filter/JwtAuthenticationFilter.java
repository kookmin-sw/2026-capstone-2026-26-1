package backend.capstone.auth.filter;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.auth.exception.AuthErrorCode;
import backend.capstone.auth.jwt.TokenStatus;
import backend.capstone.auth.jwt.service.JwtTokenProvider;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import backend.capstone.global.exception.BusinessException;
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

        TokenStatus tokenStatus = tokenProvider.validateToken(token);

        if (tokenStatus == TokenStatus.VALID) {
            try {
                Long userId = tokenProvider.getUserIdFromAccessToken(token);
                User user = userService.findById(userId);

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(new UserPrincipal(userId), null,
                        List.of());

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (BusinessException e) {
                if (e.getErrorCode() instanceof AuthErrorCode authErrorCode) {
                    request.setAttribute("AUTH_ERROR_CODE", authErrorCode);
                }
            }

        } else {
            SecurityContextHolder.clearContext();
            log.info("[JWT] 토큰 문제 발생: url={}, status={}", request.getRequestURI(), tokenStatus);

            if (tokenStatus == TokenStatus.EXPIRED) {
                request.setAttribute("AUTH_ERROR_CODE", AuthErrorCode.ACCESS_TOKEN_EXPIRED);
            } else if (tokenStatus == TokenStatus.MISSING_TOKEN) {
                request.setAttribute("AUTH_ERROR_CODE", AuthErrorCode.MISSING_ACCESS_TOKEN);
            } else {
                request.setAttribute("AUTH_ERROR_CODE", AuthErrorCode.INVALID_ACCESS_TOKEN);
            }
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
