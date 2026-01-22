package backend.capstone.auth.controller;

import backend.capstone.auth.dto.KakaoLoginRequest;
import backend.capstone.auth.dto.LoginResponse;
import backend.capstone.auth.dto.TokenPair;
import backend.capstone.auth.jwt.service.JwtTokenProvider;
import backend.capstone.auth.service.AuthService;
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
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login/kakao")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse kakaoLogin(@RequestBody KakaoLoginRequest request) {
        return authService.kakaoLogin(request.kakaoAccessToken());
    }

    @PostMapping("/refresh")
    public TokenPair refresh(@RequestHeader(value = "X-Refresh-Token") String refreshToken) {
        return authService.refreshAccessToken(refreshToken);
    }

    @GetMapping("/test")
    public String issueTestJwt() {
        return jwtTokenProvider.createAccessToken(1L);
    }

}
