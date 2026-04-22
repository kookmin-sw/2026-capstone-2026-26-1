package backend.capstone.domain.ongoingstay.service;

import backend.capstone.domain.dayroute.entity.AnalysisStatus;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.ongoingstay.entity.OngoingStay;
import backend.capstone.domain.ongoingstay.repository.OngoingStayRepository;
import backend.capstone.domain.ongoingstay.service.dto.PlaceSearchResult;
import backend.capstone.domain.place.service.PlaceService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class StayAnalysisService {

    private static final int STAY_RADIUS_METER = 50;
    private static final int STAY_MIN_DURATION_MINUTE = 15;

    private final OngoingStayRepository ongoingStayRepository;
    private final GpsPointService gpsPointService;
    private final DayRouteService dayRouteService;
    private final PlaceService placeService;
    private final PlaceSearchService placeSearchService;

    @Transactional
    public void analyzeStay(Long dayRouteId) {
        DayRoute dayRoute = dayRouteService.getDayRouteById(dayRouteId);
        // 이미 분석이 진행 중인 경우, 중복 분석 방지
        if (dayRoute.getAnalysisStatus() == AnalysisStatus.IN_PROGRESS) {
            return;
        }

        dayRoute.markInProgressAnalysis();

        //이 dayRoute에 현재 진행 중인 stay가 있는지 조회
        OngoingStay stay = ongoingStayRepository.findByDayRoute(dayRoute)
            .orElse(null);

        List<GpsPoint> newPoints = gpsPointService.getNewPoints(dayRoute,
            dayRoute.getLastAnalyzedAt());

        // 새 점이 없을 때: 마지막 ongoing stay tail 처리
        if (newPoints.isEmpty()) {
            handleLastTailIfDayEnded(dayRoute, stay);
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
            if (distance <= STAY_RADIUS_METER) {
                stay.addPoint(point);
                continue;
            }

            //종료된 stay가 10분이상 체류한 stay인지 판단
            if (stay.getDurationMinutes() >= STAY_MIN_DURATION_MINUTE) {
                promoteStayToPlace(dayRoute, stay.getCenterLatitude(), stay.getCenterLongitude());
            }

            ongoingStayRepository.delete(stay);
            stay = OngoingStay.start(dayRoute, point);
            ongoingStayRepository.save(stay);

        }
        dayRoute.completeAnalysis(newPoints.getLast().getRecordedAt());
    }

    public void promoteStayToPlace(DayRoute dayRoute, double stayLatitude, double stayLongitude) {
        Optional<PlaceSearchResult> searchResult = Optional.empty();

        try {
            searchResult = placeSearchService.searchByCoordinate(stayLatitude, stayLongitude);
        } catch (WebClientException e) {
            log.error(
                "카카오 플레이스 조회에 실패. 알 수 없는 place로 저장. dayRouteId={}, lat={}, lon={}",
                dayRoute.getId(), stayLatitude, stayLongitude, e);
        }

        placeService.saveAutoPlace(dayRoute, stayLatitude, stayLongitude, searchResult);
    }

    private void handleLastTailIfDayEnded(DayRoute dayRoute, OngoingStay stay) {
        if (stay == null) {
            return;
        }

        LocalDateTime dayRouteEndTime = dayRouteService.getDayRouteEndTime(dayRoute);
        // 아직 이 dayRoute의 종료 기준 시각이 지나지 않았으면 아무것도 하지 않음
        if (LocalDateTime.now().isBefore(dayRouteEndTime)) {
            return;
        }

        if (Duration.between(stay.getStartTime(), dayRouteEndTime).toMinutes()
            >= STAY_MIN_DURATION_MINUTE) {
            promoteStayToPlace(dayRoute, stay.getCenterLatitude(), stay.getCenterLongitude());
        }
        ongoingStayRepository.delete(stay);
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
