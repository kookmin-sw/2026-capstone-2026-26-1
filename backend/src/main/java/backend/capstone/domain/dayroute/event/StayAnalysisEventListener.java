package backend.capstone.domain.dayroute.event;

import backend.capstone.domain.ongoingstay.service.StayAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class StayAnalysisEventListener {

    private final StayAnalysisService stayAnalysisService;

    @Async("stayAnalysisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GpsPointsUploadedEvent event) {
        try {
            stayAnalysisService.analyzeStay(event.dayRouteId());
        } catch (Exception e) {
            log.error("Stay analysis failed. dayRouteId={}", event.dayRouteId(), e);
        }
    }
}
