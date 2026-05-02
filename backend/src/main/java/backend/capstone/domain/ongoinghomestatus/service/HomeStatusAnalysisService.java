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
import backend.capstone.global.util.GeoUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeStatusAnalysisService {

    private static final int INITIAL_HOME_RADIUS_METER = 100;
    private static final int ENTER_HOME_RADIUS_METER = 80;
    private static final int EXIT_HOME_RADIUS_METER = 120;
    private static final int TRANSITION_MINUTES = 3;

    private final DayRouteService dayRouteService;
    private final GpsPointService gpsPointService;
    private final BookmarkPlaceRepository bookmarkPlaceRepository;
    private final OngoingHomeStatusRepository ongoingHomeStatusRepository;

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
        List<GpsPoint> newPoints = gpsPointService.getNewPoints(
            dayRoute, dayRoute.getHomeAnalysisLastPointAt());

        if (newPoints.isEmpty()) {
            return;
        }

        OngoingHomeStatus ongoingHomeStatus = ongoingHomeStatusRepository.findByDayRoute(dayRoute)
            .orElse(null);

        int startIndex = 0;
        Instant previousPointAt = dayRoute.getHomeAnalysisLastPointAt();
        long additionalOutingSeconds = 0;

        //초기화에 사용한 첫 point를 기준점으로 삼고,
        //루프에서는 그 다음 point부터 처리
        if (ongoingHomeStatus == null) {
            GpsPoint firstPoint = newPoints.getFirst();
            ongoingHomeStatus = initializeHomeStatus(dayRoute, firstPoint, homeBookmark);
            previousPointAt = firstPoint.getRecordedAt();
            startIndex = 1;
        }

        //새 point들을 순서대로 처리
        for (int i = startIndex; i < newPoints.size(); i++) {
            GpsPoint point = newPoints.get(i);
            additionalOutingSeconds += processPoint(dayRoute, ongoingHomeStatus,
                homeBookmark, point, previousPointAt);
            previousPointAt = point.getRecordedAt();
        }

        dayRoute.addOutingDurationSeconds(additionalOutingSeconds);
        updateHomeAnalysisCursor(dayRoute, newPoints.getLast());
    }

    private OngoingHomeStatus initializeHomeStatus(DayRoute dayRoute, GpsPoint firstPoint,
        BookmarkPlace homeBookmark
    ) {
        HomeZoneStatus initialZoneStatus = determineInitialZone(firstPoint, homeBookmark);
        OngoingHomeStatus ongoingHomeStatus = OngoingHomeStatus.initialize(dayRoute, firstPoint,
            initialZoneStatus
        );

        applyInitialDayRouteStatus(dayRoute, initialZoneStatus);

        return ongoingHomeStatusRepository.save(ongoingHomeStatus);
    }

    private long processPoint(DayRoute dayRoute, OngoingHomeStatus ongoingHomeStatus,
        BookmarkPlace homeBookmark, GpsPoint point, Instant previousPointAt
    ) {
        //이번 point가 집 안인지 밖인지 판단
        HomeZoneStatus observedZoneStatus = determineObservedZone(point, homeBookmark,
            ongoingHomeStatus.getCurrentZoneStatus()
        );

        //이번 point까지의 시간 구간을 외출 시간에 반영
        long additionalOutingSeconds = calculateOutingDurationSeconds(ongoingHomeStatus,
            observedZoneStatus,
            previousPointAt, point.getRecordedAt());

        if (observedZoneStatus == ongoingHomeStatus.getCurrentZoneStatus()) {
            ongoingHomeStatus.clearCandidate();
            return additionalOutingSeconds;
        }

        if ((ongoingHomeStatus.getCandidateZoneStatus() == null)
            || (ongoingHomeStatus.getCandidateZoneStatus() != observedZoneStatus)) {
            ongoingHomeStatus.startCandidate(observedZoneStatus, point.getRecordedAt());
            return additionalOutingSeconds;
        }

        long candidateDurationMinutes = Duration.between(
            ongoingHomeStatus.getCandidateStartedAt(),
            point.getRecordedAt()
        ).toMinutes();

        if (candidateDurationMinutes >= TRANSITION_MINUTES) {
            Instant transitionTime = ongoingHomeStatus.getCandidateStartedAt();

            ongoingHomeStatus.changeCurrentZoneStatus(observedZoneStatus, transitionTime);
            applyTransitionedDayRouteStatus(dayRoute, observedZoneStatus, transitionTime);

            //외출 확정됐을 때 외출 candidate 시간 누적
            if (observedZoneStatus == HomeZoneStatus.OUT_HOME) {
                additionalOutingSeconds += Duration.between(transitionTime,
                    point.getRecordedAt()).getSeconds();
            }
        }

        return additionalOutingSeconds;
    }

    //직전 point부터 이번 point까지의 시간 구간을 외출시간에 더할지 말지를 결정
    private long calculateOutingDurationSeconds(OngoingHomeStatus ongoingHomeStatus,
        HomeZoneStatus observedZoneStatus, Instant previousPointAt, Instant currentPointAt
    ) {
        //비교할 이전 시간이 없거나
        //현재 외출 상태가 아니라면 누적 안함
        if (previousPointAt == null
            || ongoingHomeStatus.getCurrentZoneStatus() != HomeZoneStatus.OUT_HOME) {
            return 0;
        }

        //현재 확정 상태는 집 밖인데 귀가 candidate가 생긴 상태라면
        //아직 귀가 확정 전이므로 일단 보수적으로 처리
        if (ongoingHomeStatus.getCandidateZoneStatus() == HomeZoneStatus.IN_HOME) {
            //candidate 시작 시점부터 지금까지를 한 번에 외출시간으로 복구 -> 외출 시간에 누적
            if (observedZoneStatus == HomeZoneStatus.OUT_HOME) {
                return Duration.between(ongoingHomeStatus.getCandidateStartedAt(), currentPointAt
                ).getSeconds();
            }
            return 0;
        }

        //현재 확정 상태는 집 밖, 귀가 candidate도 없음 -> 외출 시간에 누적
        return Duration.between(previousPointAt, currentPointAt).getSeconds();
    }

    private void updateHomeAnalysisCursor(DayRoute dayRoute, GpsPoint lastPoint) {
        dayRoute.updateHomeAnalysisLastPointAt(lastPoint.getRecordedAt());
    }

    private HomeZoneStatus determineInitialZone(GpsPoint point, BookmarkPlace homeBookmark) {
        double distance = GeoUtils.distanceMeter(point.getLatitude(), point.getLongitude(),
            homeBookmark.getLatitude(), homeBookmark.getLongitude()
        );

        if (distance <= INITIAL_HOME_RADIUS_METER) {
            return HomeZoneStatus.IN_HOME;
        }

        return HomeZoneStatus.OUT_HOME;
    }

    private HomeZoneStatus determineObservedZone(GpsPoint point, BookmarkPlace homeBookmark,
        HomeZoneStatus currentZoneStatus
    ) {
        double distance = GeoUtils.distanceMeter(point.getLatitude(), point.getLongitude(),
            homeBookmark.getLatitude(), homeBookmark.getLongitude()
        );

        if (currentZoneStatus == HomeZoneStatus.IN_HOME) {
            if (distance < EXIT_HOME_RADIUS_METER) {
                return HomeZoneStatus.IN_HOME;
            }

            return HomeZoneStatus.OUT_HOME;
        }

        if (distance <= ENTER_HOME_RADIUS_METER) {
            return HomeZoneStatus.IN_HOME;
        }

        return HomeZoneStatus.OUT_HOME;
    }

    private void applyInitialDayRouteStatus(DayRoute dayRoute, HomeZoneStatus zoneStatus) {
        if (zoneStatus == HomeZoneStatus.IN_HOME) {
            dayRoute.markAtHome();
            return;
        }

        dayRoute.markOutingWithoutTime();
    }

    private void applyTransitionedDayRouteStatus(
        DayRoute dayRoute,
        HomeZoneStatus zoneStatus,
        Instant transitionTime
    ) {
        if (zoneStatus == HomeZoneStatus.IN_HOME) {
            dayRoute.markReturnedHome(transitionTime);
            return;
        }

        dayRoute.markOuting(transitionTime);
    }
}
