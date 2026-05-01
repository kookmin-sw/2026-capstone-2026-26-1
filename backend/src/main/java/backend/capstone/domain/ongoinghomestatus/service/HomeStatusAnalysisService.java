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

        List<GpsPoint> newPoints = gpsPointService.getNewPoints(
            dayRoute,
            dayRoute.getHomeAnalysisLastPointAt()
        );
        if (newPoints.isEmpty()) {
            return;
        }

        OngoingHomeStatus ongoingHomeStatus = ongoingHomeStatusRepository.findByDayRoute(dayRoute)
            .orElseGet(() -> initializeHomeStatus(dayRoute, newPoints.getFirst(), homeBookmark));

        for (GpsPoint point : newPoints) {
            processPoint(dayRoute, ongoingHomeStatus, homeBookmark, point);
        }

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

    private void processPoint(DayRoute dayRoute, OngoingHomeStatus ongoingHomeStatus,
        BookmarkPlace homeBookmark, GpsPoint point
    ) {
        HomeZoneStatus observedZoneStatus = determineObservedZone(point, homeBookmark,
            ongoingHomeStatus.getCurrentZoneStatus()
        );

        if (observedZoneStatus == ongoingHomeStatus.getCurrentZoneStatus()) {
            ongoingHomeStatus.clearCandidate();
            return;
        }

        if ((ongoingHomeStatus.getCandidateZoneStatus() == null)
            || (ongoingHomeStatus.getCandidateZoneStatus() != observedZoneStatus)) {
            ongoingHomeStatus.startCandidate(observedZoneStatus, point.getRecordedAt());
            return;
        }

        long candidateDurationMinutes = Duration.between(
            ongoingHomeStatus.getCandidateStartedAt(),
            point.getRecordedAt()
        ).toMinutes();

        if (candidateDurationMinutes >= TRANSITION_MINUTES) {
            Instant transitionTime = ongoingHomeStatus.getCandidateStartedAt();

            ongoingHomeStatus.changeCurrentZoneStatus(observedZoneStatus, transitionTime);
            applyTransitionedDayRouteStatus(dayRoute, observedZoneStatus, transitionTime);
        }
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

    private void applyTransitionedDayRouteStatus(DayRoute dayRoute, HomeZoneStatus zoneStatus,
        Instant transitionTime
    ) {
        if (zoneStatus == HomeZoneStatus.IN_HOME) {
            dayRoute.markReturnedHome(transitionTime);
            return;
        }

        dayRoute.markOuting(transitionTime);
    }
}
