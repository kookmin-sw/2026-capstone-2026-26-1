package backend.capstone.domain.ongoingstay.service;

import backend.capstone.domain.dayroute.entity.AnalysisStatus;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.ongoingstay.entity.OngoingStay;
import backend.capstone.domain.ongoingstay.repository.OngoingStayRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StayAnalysisService {

    private final OngoingStayRepository ongoingStayRepository;
    private final GpsPointService gpsPointService;

    @Transactional
    public void analyzeStay(DayRoute dayRoute) {
        // 이미 분석이 진행 중인 경우, 중복 분석 방지
        if (dayRoute.getAnalysisStatus() == AnalysisStatus.IN_PROGRESS) {
            return;
        }

        dayRoute.markInProgressAnalysis();

        //이 dayRoute에 현재 진행 중인 stay가 있는지 조회
        OngoingStay stay = ongoingStayRepository.findByDayRoute(dayRoute)
            .orElse(null);

        Long lastAnalyzedPointId = dayRoute.getLastAnalyzedPointId();
        List<GpsPoint> newPoints = gpsPointService.getNewPoints(dayRoute,
            lastAnalyzedPointId == null ? 0L : lastAnalyzedPointId);

        if (newPoints.isEmpty()) { //새로 분석할 gpsPoint가 없으면 바로 종료
            dayRoute.markIdleAnalysis();
            return;
        }

        for (GpsPoint point : newPoints) {
            if (stay == null) { //현재 진행 중인 stay가 없으면 현재 point로 새 stay를 시작
                stay = OngoingStay.start(dayRoute, point);
                ongoingStayRepository.save(stay);
                continue;
            }

            //현재 point가 ongoing stay 중심점에서 몇 m 떨어져 있는지 계산
            double distance = distanceMeter(stay.getCenterLatitude(), stay.getCenterLongitude(),
                point.getLatitude(),
                point.getLongitude());

            //현재 point가 기존 stay 중심에서 50m 이내면 같은 체류장소로 판단
            if (distance <= 50) {
                stay.addPoint(point);
                continue;
            }

            //종료된 stay가 10분이상 체류한 stay인지 판단
            if (stay.getDurationMinutes() >= 10) {
                //TODO: place 승격, 장소 조회
            }
            ongoingStayRepository.delete(stay);
            stay = OngoingStay.start(dayRoute, point);
            ongoingStayRepository.save(stay);

            GpsPoint lastPoint = newPoints.get(newPoints.size() - 1);
            dayRoute.completeAnalysis(lastPoint.getId());
        }
    }

    //두 좌표간 거리를 m 단위로 계산하는 함수 (하버사인 공식)
    private double distanceMeter(
        double lat1, double lon1,
        double lat2, double lon2
    ) {
        double earthRadius = 6371000;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

}
