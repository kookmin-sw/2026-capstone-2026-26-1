package backend.capstone.domain.ongoingstay.event;

import backend.capstone.domain.dayroute.service.DayRouteAnalysisLockService;
import backend.capstone.domain.ongoingstay.repository.OngoingStayRepository;
import backend.capstone.domain.ongoingstay.service.StayAnalysisService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OngoingStayTailScheduler {

    private final OngoingStayRepository ongoingStayRepository;
    private final DayRouteAnalysisLockService dayRouteAnalysisLockService;
    private final StayAnalysisService stayAnalysisService;

    @Scheduled(
        fixedDelayString = "600000", //10분, 반복 고정 실행
        initialDelayString = "60000" //1분, 서버 시작 후 첫 실행
    )
    public void reconcileTails() {
        List<Long> dayRouteIds =
            ongoingStayRepository.findDayRouteIdsWithEndedOngoingStay(Instant.now());

        for (Long dayRouteId : dayRouteIds) {
            dayRouteAnalysisLockService.withLock(dayRouteId, () -> analyzeStayTail(dayRouteId));
        }
    }

    private void analyzeStayTail(Long dayRouteId) {
        try {
            stayAnalysisService.analyzeStayTail(dayRouteId);
        } catch (Exception e) {
            log.error("Ongoing stay tail 처리 실패. dayRouteId={}", dayRouteId, e);
        }
    }
}
