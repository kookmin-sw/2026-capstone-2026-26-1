package backend.capstone.domain.user.service;

import backend.capstone.auth.service.dto.KakaoUserInfoResponse;
import backend.capstone.domain.user.entity.ProviderType;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.mapper.UserMapper;
import backend.capstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public User upsertKakaoUser(KakaoUserInfoResponse kakaoUser) {
		return userRepository.findByProviderAndProviderId(ProviderType.KAKAO, kakaoUser.id())
			.map(existing -> {
				existing.updateProfile(kakaoUser.kakao_account().profile().nickname(),
					kakaoUser.kakao_account().profile().profile_image_url());
				return existing;
			})
			.orElseGet(() -> userRepository.save(
				UserMapper.toEntity(kakaoUser))
			);
	}

}
