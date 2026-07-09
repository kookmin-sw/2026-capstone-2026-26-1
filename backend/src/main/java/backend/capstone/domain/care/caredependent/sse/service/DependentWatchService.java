package backend.capstone.domain.care.caredependent.sse.service;

import backend.capstone.domain.care.carerelationship.repository.CareRelationshipRepository;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.repository.UserRepository;
import backend.capstone.integration.fcm.client.FcmClient;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DependentWatchService {

    private static final String ACTIVE_DEPENDENTS_KEY = "watch:active-dependents";

    private final StringRedisTemplate redisTemplate;
    private final CareRelationshipRepository careRelationshipRepository;
    private final UserRepository userRepository;
    private final FcmClient fcmClient;

    public void startWatching(Long guardianUserId) {
        for (User dependent : careRelationshipRepository.findDependentUsersByGuardianUserId(
            guardianUserId)) {
            addWatcher(dependent, guardianUserId);
        }
    }

    public void stopWatching(Long guardianUserId) {
        for (User dependent : careRelationshipRepository.findDependentUsersByGuardianUserId(
            guardianUserId)) {
            removeWatcher(dependent, guardianUserId);
        }
    }

    private void addWatcher(User dependent, Long guardianUserId) {
        try {
            Long watcherCount = redisTemplate.opsForSet()
                .add(watchKey(dependent.getId()), String.valueOf(guardianUserId));
            if (watcherCount != null && watcherCount > 0
                && isFirstWatcher(dependent.getId())) {
                redisTemplate.opsForSet().add(ACTIVE_DEPENDENTS_KEY, String.valueOf(dependent.getId()));
                fcmClient.sendWatchingStatusChanged(dependent.getFcmToken(), true);
            }
        } catch (Exception e) {
            log.warn("위치 조회 watch 등록에 실패했습니다. dependentUserId={}, guardianUserId={}",
                dependent.getId(), guardianUserId, e);
        }
    }

    private void removeWatcher(User dependent, Long guardianUserId) {
        try {
            redisTemplate.opsForSet().remove(watchKey(dependent.getId()),
                String.valueOf(guardianUserId));
            if (isNoLongerWatched(dependent.getId())) {
                redisTemplate.opsForSet().remove(ACTIVE_DEPENDENTS_KEY, String.valueOf(dependent.getId()));
                fcmClient.sendWatchingStatusChanged(dependent.getFcmToken(), false);
            }
        } catch (Exception e) {
            log.warn("위치 조회 watch 해제에 실패했습니다. dependentUserId={}, guardianUserId={}",
                dependent.getId(), guardianUserId, e);
        }
    }

    public void resendWatchingSignalToActiveDependents() {
        Set<String> activeDependentIds = redisTemplate.opsForSet().members(ACTIVE_DEPENDENTS_KEY);
        if (activeDependentIds == null || activeDependentIds.isEmpty()) {
            return;
        }
        for (String dependentIdText : activeDependentIds) {
            try {
                Long dependentId = Long.valueOf(dependentIdText);
                userRepository.findById(dependentId)
                    .ifPresent(dependent -> fcmClient.sendWatchingStatusChanged(dependent.getFcmToken(), true));
            } catch (Exception e) {
                log.warn("watching 재발송에 실패했습니다. dependentUserId={}", dependentIdText, e);
            }
        }
    }

    private boolean isFirstWatcher(Long dependentUserId) {
        Long size = redisTemplate.opsForSet().size(watchKey(dependentUserId));
        return size != null && size == 1;
    }

    private boolean isNoLongerWatched(Long dependentUserId) {
        Long size = redisTemplate.opsForSet().size(watchKey(dependentUserId));
        return size == null || size == 0;
    }

    private String watchKey(Long dependentUserId) {
        return "watch:dependent:" + dependentUserId;
    }
}
