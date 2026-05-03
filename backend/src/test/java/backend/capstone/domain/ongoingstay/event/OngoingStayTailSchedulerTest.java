package backend.capstone.domain.ongoingstay.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.capstone.domain.dayroute.service.DayRouteAnalysisLockService;
import backend.capstone.domain.ongoingstay.repository.OngoingStayRepository;
import backend.capstone.domain.ongoingstay.service.StayAnalysisService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OngoingStayTailSchedulerTest {

    @Mock
    private OngoingStayRepository ongoingStayRepository;

    @Mock
    private DayRouteAnalysisLockService dayRouteAnalysisLockService;

    @Mock
    private StayAnalysisService stayAnalysisService;

    @InjectMocks
    private OngoingStayTailScheduler ongoingStayTailScheduler;

    @Test
    void 종료된_ongoingStay_dayRoute만_tail_분석을_실행한다() {
        when(ongoingStayRepository.findDayRouteIdsWithEndedOngoingStay(any(Instant.class)))
            .thenReturn(List.of(3L, 7L));
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(dayRouteAnalysisLockService).withLock(any(), any(Runnable.class));

        ongoingStayTailScheduler.reconcileTails();

        verify(ongoingStayRepository).findDayRouteIdsWithEndedOngoingStay(any(Instant.class));
        verify(dayRouteAnalysisLockService).withLock(eq(3L), any(Runnable.class));
        verify(dayRouteAnalysisLockService).withLock(eq(7L), any(Runnable.class));
        verify(stayAnalysisService).analyzeStayTail(3L);
        verify(stayAnalysisService).analyzeStayTail(7L);
    }
}
