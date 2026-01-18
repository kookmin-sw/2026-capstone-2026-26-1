package backend.capstone.domain.user;

import backend.capstone.domain.user.entity.ProviderType;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserInitializer implements ApplicationRunner {

	private final UserRepository userRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		User testUser1 = User.builder()
			.nickname("지은")
			.profileImageUrl("https://example.com/profile1.jpg")
			.provider(ProviderType.KAKAO)
			.providerId("test_kakao_1")
			.build();

		userRepository.save(testUser1);
	}
}