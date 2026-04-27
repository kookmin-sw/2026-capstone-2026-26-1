package backend.capstone.auth.controller;

import backend.capstone.auth.dto.KakaoLoginRequest;
import backend.capstone.auth.dto.LoginResponse;
import backend.capstone.auth.dto.TokenPair;
import backend.capstone.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerSpec {

    private final AuthService authService;

    @Override
    @PostMapping("/login/kakao")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse kakaoLogin(@RequestBody KakaoLoginRequest request) {
        return authService.kakaoLogin(request.kakaoAccessToken());
    }

    @Override
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenPair refresh(HttpServletRequest request,
        @RequestHeader(value = "X-Refresh-Token") String refreshToken) {
        String accessToken = resolveBearerToken(request);

        return authService.refreshAccessToken(accessToken, refreshToken);
    }

    @Override
    @GetMapping("/test-token/issue")
    public TokenPair issueTestJwt() {
        return authService.testIssue(1L);
    }

    @Override
    @GetMapping("/hyewon-token/issue")
    public TokenPair issueHyewonJwt() {
        return authService.testIssue(2L);
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
