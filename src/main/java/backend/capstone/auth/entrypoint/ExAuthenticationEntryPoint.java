package backend.capstone.auth.entrypoint;

import backend.capstone.auth.exception.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        AuthErrorCode errorCode = (AuthErrorCode) request.getAttribute("AUTH_ERROR_CODE");
        if (errorCode == null) {
            errorCode = AuthErrorCode.UNKNOWN_ERROR;
        }

        objectMapper.writeValue(response.getWriter(), Map.of(
            "code", errorCode.name(),
            "message", errorCode.getMessage()
        ));

    }
}
