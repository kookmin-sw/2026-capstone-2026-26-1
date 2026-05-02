package backend.capstone.domain.ongoinghomestatus.service;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.bookmarkplace.repository.BookmarkPlaceRepository;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.ongoinghomestatus.entity.HomeZoneStatus;
import backend.capstone.domain.ongoinghomestatus.entity.OngoingHomeStatus;
import backend.capstone.domain.ongoinghomestatus.repository.OngoingHomeStatusRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeStatusAnalysisService {

    private final DayRouteService dayRouteService;
    private final GpsPointService gpsPointService;
    private final BookmarkPlaceRepository bookmarkPlaceRepository;
    private final OngoingHomeStatusRepository ongoingHomeStatusRepository;
    private final HomeZoneDecider homeZoneDecider;
    private final OutingDurationAccumulator outingDurationAccumulator;
    private final HomeStatusTransitionHandler homeStatusTransitionHandler;

    @Transactional
    public void analyzeHomeStatus(Long dayRouteId) {
        DayRoute dayRoute = dayRouteService.getDayRouteById(dayRouteId);

        Optional<BookmarkPlace> optionalHome = bookmarkPlaceRepository.findByUserIdAndType(
            dayRoute.getUser().getId(), BookmarkPlaceType.HOME);

        if (optionalHome.isEmpty()) {
            dayRoute.markNoHomeBookmark();
            return;
        }
        BookmarkPlace homeBookmark = optionalHome.get();

        //증분 분석
        List<GpsPoint> newPoints = gpsPointService.getNewPoints(dayRoute,
            dayRoute.getHomeAnalysisLastPointAt());

        if (newPoints.isEmpty()) {
            return;
        }

        OngoingHomeStatus ongoingHomeStatus = ongoingHomeStatusRepository.findByDayRoute(dayRoute)
            .orElse(null);

        int startIndex = 0;
        Instant previousPointAt = dayRoute.getHomeAnalysisLastPointAt();
        long additionalOutingSeconds = 0;

        //초기화에 사용한 첫 point를 기준점으로 삼고, 루프에서는 그 다음 point부터 처리
        if (ongoingHomeStatus == null) {
            GpsPoint firstPoint = newPoints.getFirst();
            ongoingHomeStatus = initializeHomeStatus(dayRoute, firstPoint, homeBookmark);
            previousPointAt = firstPoint.getRecordedAt();
            startIndex = 1;
        }

        //새 point들을 순서대로 처리
        for (int i = startIndex; i < newPoints.size(); i++) {
            GpsPoint point = newPoints.get(i);
            additionalOutingSeconds += processPoint(dayRoute, ongoingHomeStatus, homeBookmark,
                point, previousPointAt);
            previousPointAt = point.getRecordedAt();
        }

        dayRoute.addOutingDurationSeconds(additionalOutingSeconds);
        dayRoute.updateHomeAnalysisLastPointAt(newPoints.getLast().getRecordedAt());
    }

    private OngoingHomeStatus initializeHomeStatus(DayRoute dayRoute, GpsPoint firstPoint,
        BookmarkPlace homeBookmark) {
        HomeZoneStatus initialZoneStatus = homeZoneDecider.determineInitialZone(firstPoint,
            homeBookmark);
        OngoingHomeStatus ongoingHomeStatus = OngoingHomeStatus.initialize(dayRoute, firstPoint,
            initialZoneStatus);

        homeStatusTransitionHandler.applyInitialDayRouteStatus(dayRoute, initialZoneStatus);

        return ongoingHomeStatusRepository.save(ongoingHomeStatus);
    }

    private long processPoint(DayRoute dayRoute, OngoingHomeStatus ongoingHomeStatus,
        BookmarkPlace homeBookmark, GpsPoint point, Instant previousPointAt) {

        HomeZoneStatus observedZoneStatus = homeZoneDecider.determineObservedZone(point,
            homeBookmark, ongoingHomeStatus.getCurrentZoneStatus());

        long additionalOutingSeconds = outingDurationAccumulator.calculateSegmentOutingDurationSeconds(
            ongoingHomeStatus, observedZoneStatus, previousPointAt, point.getRecordedAt());

        additionalOutingSeconds += homeStatusTransitionHandler.handleTransition(dayRoute,
            ongoingHomeStatus, observedZoneStatus, point.getRecordedAt());

        return additionalOutingSeconds;
    }

}
