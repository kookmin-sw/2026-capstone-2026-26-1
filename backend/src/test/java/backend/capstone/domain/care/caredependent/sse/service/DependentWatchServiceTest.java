package backend.capstone.domain.care.caredependent.sse.service;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import backend.capstone.domain.care.carerelationship.repository.CareRelationshipRepository;
import backend.capstone.domain.user.entity.ProviderType;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.repository.UserRepository;
import backend.capstone.integration.fcm.client.FcmClient;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class DependentWatchServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private CareRelationshipRepository careRelationshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FcmClient fcmClient;

    @InjectMocks
    private DependentWatchService dependentWatchService;

    @Test
    void 최초_watcher_등록시_watching_true_fcm을_발송한다() {
        User dependent = createUser(10L, "fcm-token-10");
        given(careRelationshipRepository.findDependentUsersByGuardianUserId(1L))
            .willReturn(List.of(dependent));
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.add("watch:dependent:10", "1")).willReturn(1L);
        given(setOperations.size("watch:dependent:10")).willReturn(1L);

        dependentWatchService.startWatching(1L);

        then(fcmClient).should().sendWatchingStatusChanged("fcm-token-10", true);
    }

    @Test
    void 이미_다른_watcher가_있으면_중복으로_fcm을_발송하지_않는다() {
        User dependent = createUser(10L, "fcm-token-10");
        given(careRelationshipRepository.findDependentUsersByGuardianUserId(2L))
            .willReturn(List.of(dependent));
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.add("watch:dependent:10", "2")).willReturn(1L);
        given(setOperations.size("watch:dependent:10")).willReturn(2L);

        dependentWatchService.startWatching(2L);

        then(fcmClient).should(never()).sendWatchingStatusChanged(anyString(), anyBoolean());
    }

    @Test
    void 마지막_watcher_해제시_watching_false_fcm을_발송한다() {
        User dependent = createUser(10L, "fcm-token-10");
        given(careRelationshipRepository.findDependentUsersByGuardianUserId(1L))
            .willReturn(List.of(dependent));
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.size("watch:dependent:10")).willReturn(0L);

        dependentWatchService.stopWatching(1L);

        then(fcmClient).should().sendWatchingStatusChanged("fcm-token-10", false);
    }

    @Test
    void 다른_watcher가_남아있으면_복구_fcm을_발송하지_않는다() {
        User dependent = createUser(10L, "fcm-token-10");
        given(careRelationshipRepository.findDependentUsersByGuardianUserId(1L))
            .willReturn(List.of(dependent));
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.size("watch:dependent:10")).willReturn(1L);

        dependentWatchService.stopWatching(1L);

        then(fcmClient).should(never()).sendWatchingStatusChanged(anyString(), anyBoolean());
    }

    @Test
    void redis_호출이_실패해도_예외를_전파하지_않는다() {
        User dependent = createUser(10L, "fcm-token-10");
        given(careRelationshipRepository.findDependentUsersByGuardianUserId(1L))
            .willReturn(List.of(dependent));
        given(redisTemplate.opsForSet()).willThrow(new RuntimeException("redis 연결 실패"));

        dependentWatchService.startWatching(1L);

        then(fcmClient).should(never()).sendWatchingStatusChanged(anyString(), anyBoolean());
    }

    @Test
    void 최초_watcher_등록시_활성_dependent_목록에_추가한다() {
        User dependent = createUser(10L, "fcm-token-10");
        given(careRelationshipRepository.findDependentUsersByGuardianUserId(1L))
            .willReturn(List.of(dependent));
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.add("watch:dependent:10", "1")).willReturn(1L);
        given(setOperations.size("watch:dependent:10")).willReturn(1L);

        dependentWatchService.startWatching(1L);

        then(setOperations).should().add("watch:active-dependents", "10");
    }

    @Test
    void 마지막_watcher_해제시_활성_dependent_목록에서_제거한다() {
        User dependent = createUser(10L, "fcm-token-10");
        given(careRelationshipRepository.findDependentUsersByGuardianUserId(1L))
            .willReturn(List.of(dependent));
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.size("watch:dependent:10")).willReturn(0L);

        dependentWatchService.stopWatching(1L);

        then(setOperations).should().remove("watch:active-dependents", "10");
    }

    @Test
    void 활성_dependent_전체에게_watching_true를_재발송한다() {
        User dependent = createUser(10L, "fcm-token-10");
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.members("watch:active-dependents")).willReturn(Set.of("10"));
        given(userRepository.findById(10L)).willReturn(Optional.of(dependent));

        dependentWatchService.resendWatchingSignalToActiveDependents();

        then(fcmClient).should().sendWatchingStatusChanged("fcm-token-10", true);
    }

    @Test
    void 활성_dependent가_없으면_아무것도_재발송하지_않는다() {
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.members("watch:active-dependents")).willReturn(Set.of());

        dependentWatchService.resendWatchingSignalToActiveDependents();

        then(fcmClient).should(never()).sendWatchingStatusChanged(anyString(), anyBoolean());
    }

    @Test
    void 재발송_중_한_사용자_조회가_실패해도_나머지는_계속_처리한다() {
        User dependent = createUser(20L, "fcm-token-20");
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.members("watch:active-dependents")).willReturn(Set.of("invalid-id", "20"));
        given(userRepository.findById(20L)).willReturn(Optional.of(dependent));

        dependentWatchService.resendWatchingSignalToActiveDependents();

        then(fcmClient).should().sendWatchingStatusChanged("fcm-token-20", true);
    }

    private User createUser(Long userId, String fcmToken) {
        User user = User.builder()
            .provider(ProviderType.KAKAO)
            .providerId("provider-" + userId)
            .nickname("nickname-" + userId)
            .profileImageUrl("https://example.com/" + userId + ".png")
            .build();
        user.updateFcmToken(fcmToken);

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
