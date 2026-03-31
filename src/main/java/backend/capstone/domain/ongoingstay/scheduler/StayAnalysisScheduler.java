package backend.capstone.domain.ongoingstay.scheduler;

import backend.capstone.domain.dayroute.entity.AnalysisStatus;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.ongoingstay.service.StayAnalysisService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StayAnalysisScheduler {

    private final DayRouteService dayRouteService;
    private final StayAnalysisService stayAnalysisService;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void scheduleStayAnalysis() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);

        List<DayRoute> targetDayRoute = dayRouteService.getStayAnalysisTargetDayRoute(
            AnalysisStatus.IDLE,
            threshold
        );

        for (DayRoute dayRoute : targetDayRoute) {
            stayAnalysisService.analyzeStay(dayRoute);
        }
    }
}
