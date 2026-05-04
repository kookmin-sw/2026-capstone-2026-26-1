package backend.capstone.domain.ongoingstay.service;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.kakaoplace.dto.SearchResultByCategoryAndCoord;
import backend.capstone.domain.kakaoplace.service.KakaoSearchByCategoryService;
import backend.capstone.domain.ongoingstay.entity.OngoingStay;
import backend.capstone.domain.ongoingstay.repository.OngoingStayRepository;
import backend.capstone.domain.place.service.PlaceService;
import backend.capstone.global.util.GeoUtils;
import java.time.Duration;
import java.time.Instant;
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
    private static final int STAY_MIN_DURATION_MINUTE = 10;

    private final OngoingStayRepository ongoingStayRepository;
    private final GpsPointService gpsPointService;
    private final DayRouteService dayRouteService;
    private final PlaceService placeService;
    private final KakaoSearchByCategoryService kakaoSearchByCategoryService;

    @Transactional
    public void analyzeStay(Long dayRouteId) {
        DayRoute dayRoute = dayRouteService.getDayRouteById(dayRouteId);

        //이 dayRoute에 현재 진행 중인 stay가 있는지 조회
        OngoingStay stay = ongoingStayRepository.findByDayRoute(dayRoute)
            .orElse(null);

        List<GpsPoint> newPoints = gpsPointService.getNewPoints(dayRoute,
            dayRoute.getLastAnalyzedAt());

        if (newPoints.isEmpty()) {
            return;
        }

        for (GpsPoint point : newPoints) {
            if (stay == null) { //현재 진행 중인 stay가 없으면 현재 point로 새 stay를 시작
                stay = OngoingStay.start(dayRoute, point);
                ongoingStayRepository.save(stay);
                continue;
            }

            //현재 point가 ongoing stay 중심점에서 몇 m 떨어져 있는지 계산
            double distance = GeoUtils.distanceMeter(
                stay.getCenterLatitude(), stay.getCenterLongitude(),
                point.getLatitude(), point.getLongitude());

            //현재 point가 기존 stay 중심에서 50m 이내면 같은 체류장소로 판단
            if (distance <= STAY_RADIUS_METER) {
                stay.addPoint(point);
                continue;
            }

            //종료된 stay가 15분이상 체류한 stay인지 판단
            if (stay.getDurationMinutes() >= STAY_MIN_DURATION_MINUTE) {
                promoteStayToPlace(dayRoute, stay);
            }

            ongoingStayRepository.delete(stay);
            stay = OngoingStay.start(dayRoute, point);
            ongoingStayRepository.save(stay);
        }

        dayRoute.completeAnalysis(newPoints.getLast().getRecordedAt());
    }

    public void promoteStayToPlace(DayRoute dayRoute, OngoingStay stay) {
        Optional<SearchResultByCategoryAndCoord> searchResult = Optional.empty();

        try {
            searchResult = kakaoSearchByCategoryService.searchByCategory(stay.getCenterLatitude(),
                stay.getCenterLongitude(), dayRoute.getUser().getId());
        } catch (WebClientException e) {
            log.error(
                "카카오 장소 조회에 실패했습니다. 이름 없는 장소로 저장합니다. dayRouteId={}, lat={}, lon={}",
                dayRoute.getId(), stay.getCenterLatitude(), stay.getCenterLongitude(), e);
        }

        placeService.saveAutoPlace(dayRoute, stay, searchResult);
    }

    @Transactional
    public void analyzeStayTail(Long dayRouteId) {
        DayRoute dayRoute = dayRouteService.getDayRouteById(dayRouteId);

        OngoingStay stay = ongoingStayRepository.findByDayRoute(dayRoute)
            .orElse(null);

        if (stay == null) {
            return;
        }

        Instant dayRouteEndTime = dayRoute.getEndTime();

        if (Duration.between(stay.getStartTime(), dayRouteEndTime).toMinutes()
            >= STAY_MIN_DURATION_MINUTE) {
            stay.updateLastTime(dayRouteEndTime);
            promoteStayToPlace(dayRoute, stay);
        }

        ongoingStayRepository.delete(stay);
    }
}
