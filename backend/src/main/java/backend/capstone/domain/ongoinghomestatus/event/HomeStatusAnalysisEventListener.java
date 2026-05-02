package backend.capstone.domain.ongoinghomestatus.event;

import backend.capstone.domain.dayroute.event.GpsPointsUploadedEvent;
import backend.capstone.domain.ongoinghomestatus.service.HomeStatusAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class HomeStatusAnalysisEventListener {

    private final HomeStatusAnalysisService homeStatusAnalysisService;

    @Async("homeStatusAnalysisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GpsPointsUploadedEvent event) {
        try {
            homeStatusAnalysisService.analyzeHomeStatus(event.dayRouteId());
        } catch (Exception e) {
            log.error("Home status analysis failed. dayRouteId={}", event.dayRouteId(), e);
        }
    }
}
