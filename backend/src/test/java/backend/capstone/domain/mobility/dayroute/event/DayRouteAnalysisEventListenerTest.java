package backend.capstone.domain.mobility.dayroute.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import backend.capstone.domain.mobility.analysis.listener.DayRouteAnalysisEventListener;
import backend.capstone.domain.mobility.analysis.ongoinghomestatus.service.HomeStatusAnalysisService;
import backend.capstone.domain.mobility.analysis.ongoingstay.service.StayAnalysisService;
import backend.capstone.domain.mobility.analysis.service.DayRouteAnalysisLockService;
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
    void 락안에서_홈분석후_스테이분석을_순서대로_실행한다() {
        Long dayRouteId = 3L;
        GpsPointsUploadedEvent event = new GpsPointsUploadedEvent(dayRouteId);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(dayRouteAnalysisLockService)
            .withLock(eq(dayRouteId), org.mockito.ArgumentMatchers.any(Runnable.class));

        dayRouteAnalysisEventListener.handle(event);

        verify(dayRouteAnalysisLockService).withLock(eq(dayRouteId),
            org.mockito.ArgumentMatchers.any(Runnable.class));

        InOrder inOrder = inOrder(homeStatusAnalysisService, stayAnalysisService);
        inOrder.verify(homeStatusAnalysisService).analyzeHomeStatus(dayRouteId);
        inOrder.verify(stayAnalysisService).analyzeStay(dayRouteId);
    }
}
