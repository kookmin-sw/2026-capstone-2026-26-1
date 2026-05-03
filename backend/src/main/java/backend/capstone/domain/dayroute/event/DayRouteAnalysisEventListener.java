package backend.capstone.domain.dayroute.event;

import backend.capstone.domain.dayroute.service.DayRouteAnalysisLockService;
import backend.capstone.domain.ongoinghomestatus.service.HomeStatusAnalysisService;
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
public class DayRouteAnalysisEventListener {

    private final DayRouteAnalysisLockService dayRouteAnalysisLockService;
    private final StayAnalysisService stayAnalysisService;
    private final HomeStatusAnalysisService homeStatusAnalysisService;

    @Async("homeStatusAnalysisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GpsPointsUploadedEvent event) {
        //dayRouteId 키로 락을 잡은 상태에서 home 분석과 stay 분석을 순차적으로 실행
        dayRouteAnalysisLockService.withLock(event.dayRouteId(), () -> {
            analyzeHomeStatus(event.dayRouteId());
            analyzeStay(event.dayRouteId());
        });
    }

    private void analyzeHomeStatus(Long dayRouteId) {
        try {
            homeStatusAnalysisService.analyzeHomeStatus(dayRouteId);
        } catch (Exception e) {
            log.error("Home 분석 실패. dayRouteId={}", dayRouteId, e);
        }
    }

    private void analyzeStay(Long dayRouteId) {
        try {
            stayAnalysisService.analyzeStay(dayRouteId);
        } catch (Exception e) {
            log.error("Stay 분석 실패. dayRouteId={}", dayRouteId, e);
        }
    }

}
