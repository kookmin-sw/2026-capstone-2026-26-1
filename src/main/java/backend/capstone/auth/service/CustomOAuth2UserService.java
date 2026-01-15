package backend.capstone.auth.service;

import backend.capstone.auth.service.principal.UserPrincipal;
import backend.capstone.auth.util.OAuthAttributes;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.mapper.UserMapper;
import backend.capstone.domain.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		// 유저 정보 획득
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		// OAuth2 제공자(provider) 확인
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		if (!"kakao".equals(registrationId)) {
			throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다.");
		}

		// attributes 파싱
		Map<String, Object> attributes = oAuth2User.getAttributes(); //파싱
		OAuthAttributes oa = OAuthAttributes.ofKakao(attributes);//정제

		// db 조회 -> 없으면 생성, 있으면 회원 정보 업데이트
		User user = userRepository.findByProviderAndProviderId(oa.getProvider(), oa.getProviderId())
			.map(existing -> {
//				existing.updateProfile(oa.getNickname(), oa.getProfileImageUrl()); //TODO: 프로필 업데이트 기능 구현
				return existing;
			})
			.orElseGet(() -> userRepository.save(
				UserMapper.toEntity(oa))
			);

		return new UserPrincipal(user.getId(), attributes);
	}
}
