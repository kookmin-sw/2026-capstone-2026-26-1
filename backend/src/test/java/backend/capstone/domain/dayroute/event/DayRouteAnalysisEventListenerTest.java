package backend.capstone.domain.dayroute.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import backend.capstone.domain.dayroute.service.DayRouteAnalysisLockService;
import backend.capstone.domain.ongoinghomestatus.service.HomeStatusAnalysisService;
import backend.capstone.domain.ongoingstay.service.StayAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DayRouteAnalysisEventListenerTest {

    @Mock
    private DayRouteAnalysisLockService dayRouteAnalysisLockService;

    @Mock
    private StayAnalysisService stayAnalysisService;

    @Mock
    private HomeStatusAnalysisService homeStatusAnalysisService;

    @InjectMocks
    private DayRouteAnalysisEventListener dayRouteAnalysisEventListener;

    @Test
    void 락안에서_스테이분석후_홈분석을_순서대로_실행한다() {
        Long dayRouteId = 3L;
        GpsPointsUploadedEvent event = new GpsPointsUploadedEvent(dayRouteId);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(dayRouteAnalysisLockService).withLock(eq(dayRouteId), org.mockito.ArgumentMatchers.any(Runnable.class));

        dayRouteAnalysisEventListener.handle(event);

        verify(dayRouteAnalysisLockService).withLock(eq(dayRouteId),
            org.mockito.ArgumentMatchers.any(Runnable.class));

        InOrder inOrder = inOrder(stayAnalysisService, homeStatusAnalysisService);
        inOrder.verify(stayAnalysisService).analyzeStay(dayRouteId);
        inOrder.verify(homeStatusAnalysisService).analyzeHomeStatus(dayRouteId);
    }
}
