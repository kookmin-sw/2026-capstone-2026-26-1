package backend.capstone.domain.ongoinghomestatus.service;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.bookmarkplace.repository.BookmarkPlaceRepository;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.ongoinghomestatus.entity.OngoingHomeStatus;
import backend.capstone.domain.ongoinghomestatus.repository.OngoingHomeStatusRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeStatusAnalysisService {

    private static final int HOME_RADIUS_METER = 100;
    private static final int TRANSITION_MINUTES = 5;

    private final DayRouteService dayRouteService;
    private final GpsPointService gpsPointService;
    private final BookmarkPlaceRepository bookmarkPlaceRepository;
    private final OngoingHomeStatusRepository ongoingHomeStatusRepository;

    @Transactional
    public void analyzeHomeStatus(Long dayRouteId) {
        DayRoute dayRoute = dayRouteService.getDayRouteById(dayRouteId);

        Optional<BookmarkPlace> optionalHome =
            bookmarkPlaceRepository.findByUserIdAndType(
                dayRoute.getUser().getId(), BookmarkPlaceType.HOME);

        //집 주소 등록이 안된 경우
        if (optionalHome.isEmpty()) {
            handleNoHomeBookmark(dayRoute); //TODO: 집 주소 등록이 안된 경우는 현재 메서드가 아예 실행안되도록 바꿔볼까
            return;
        }
        BookmarkPlace homeBookmark = optionalHome.get();

        //새롭게 들어온 gps point들을 가져옴
        List<GpsPoint> newPoints = getNewHomeAnalysisPoints(dayRoute);
        if (newPoints.isEmpty()) {
            return;
        }

        OngoingHomeStatus ongoingHomeStatus = ongoingHomeStatusRepository.findByDayRoute(dayRoute)
            .orElseGet(() -> initializeHomeStatus(dayRoute, newPoints.getFirst(), homeBookmark));

        for (GpsPoint point : newPoints) {
            processPoint(dayRoute, ongoingHomeStatus, homeBookmark, point);
        }

        //커서 업데이트
        updateHomeAnalysisCursor(dayRoute, newPoints.getLast());
    }

    private void handleNoHomeBookmark(DayRoute dayRoute) {
        dayRoute.markNoHomeBookmark();
    }

    private List<GpsPoint> getNewHomeAnalysisPoints(DayRoute dayRoute) {
        return gpsPointService.getNewPoints(dayRoute, dayRoute.getHomeAnalysisLastPointAt());
    }

    private OngoingHomeStatus initializeHomeStatus(
        DayRoute dayRoute,
        GpsPoint firstPoint,
        BookmarkPlace homeBookmark
    ) {
        throw new UnsupportedOperationException("initializeHomeStatus will be implemented next");
    }

    private void processPoint(
        DayRoute dayRoute,
        OngoingHomeStatus ongoingHomeStatus,
        BookmarkPlace homeBookmark,
        GpsPoint point
    ) {
        throw new UnsupportedOperationException("processPoint will be implemented next");
    }

    private void updateHomeAnalysisCursor(DayRoute dayRoute, GpsPoint lastPoint) {
        dayRoute.updateHomeAnalysisLastPointAt(lastPoint.getRecordedAt());
    }
}
