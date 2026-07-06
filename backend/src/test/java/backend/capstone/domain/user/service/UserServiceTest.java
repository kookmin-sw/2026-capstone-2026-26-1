package backend.capstone.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import backend.capstone.domain.user.entity.ProviderType;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void fcm_토큰을_갱신한다() {
        User user = createUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        userService.updateFcmToken(1L, "new-fcm-token");

        assertThat(user.getFcmToken()).isEqualTo("new-fcm-token");
    }

    private User createUser(Long userId) {
        User user = User.builder()
            .provider(ProviderType.KAKAO)
            .providerId("provider-" + userId)
            .nickname("nickname-" + userId)
            .profileImageUrl("https://example.com/" + userId + ".png")
            .build();

        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, userId);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("사용자 ID 설정에 실패했습니다.", e);
        }

        return user;
    }
}
