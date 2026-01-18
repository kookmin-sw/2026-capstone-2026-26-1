package backend.capstone.auth.service;

import backend.capstone.auth.dto.LoginResponse;
import backend.capstone.auth.jwt.service.JwtTokenProvider;
import backend.capstone.auth.service.client.KakaoApiClient;
import backend.capstone.auth.service.dto.KakaoUserInfoResponse;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final KakaoApiClient kakaoApiClient;
	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public LoginResponse kakaoLogin(String kakaoAccessToken) {
		KakaoUserInfoResponse kakaoUser = kakaoApiClient.getUserInfo(kakaoAccessToken);
		User user = userService.upsertKakaoUser(kakaoUser);
		String accessToken = jwtTokenProvider.createAccessToken(user.getId());
		return new LoginResponse(user.getId(), user.getNickname(), user.getProfileImageUrl(),
			accessToken);
	}


}
