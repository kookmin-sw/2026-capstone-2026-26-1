package backend.capstone.domain.care.caredependent.sse.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DependentWatchingResendSchedulerTest {

    @Mock
    private DependentWatchService dependentWatchService;

    @InjectMocks
    private DependentWatchingResendScheduler dependentWatchingResendScheduler;

    @Test
    void 재발송_스케줄러가_서비스에_재발송을_위임한다() {
        dependentWatchingResendScheduler.resendWatchingSignal();

        verify(dependentWatchService).resendWatchingSignalToActiveDependents();
    }
}
