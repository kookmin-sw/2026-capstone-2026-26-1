package backend.capstone.domain.care.caredependent.sse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DependentWatchingResendScheduler {

    private final DependentWatchService dependentWatchService;

    @Scheduled(fixedDelayString = "300000", initialDelayString = "300000") // 5분
    public void resendWatchingSignal() {
        dependentWatchService.resendWatchingSignalToActiveDependents();
    }
}
