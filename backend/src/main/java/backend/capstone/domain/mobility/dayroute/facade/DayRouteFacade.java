package backend.capstone.domain.mobility.dayroute.facade;

import backend.capstone.domain.care.caredependent.sse.service.CareLocationSseEventService;
import backend.capstone.domain.mobility.analysis.visitedregion.service.VisitedRegionService;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteBookmarkBatchRequest;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteBookmarkListResponse;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteBookmarkResponse;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteMemoRequest;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteMemoResponse;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteMonthlyResponse;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteSummaryResponse;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteTitleRequest;
import backend.capstone.domain.mobility.dayroute.dto.DayRouteTitleResponse;
import backend.capstone.domain.mobility.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.mobility.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.mobility.dayroute.entity.DayRoute;
import backend.capstone.domain.mobility.dayroute.event.GpsPointsUploadedEvent;
import backend.capstone.domain.mobility.dayroute.exception.DayRouteErrorCode;
import backend.capstone.domain.mobility.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.mobility.dayroute.service.DayRouteService;
import backend.capstone.domain.mobility.gpspoint.entity.GpsPoint;
import backend.capstone.domain.mobility.gpspoint.service.GpsPointService;
import backend.capstone.domain.mobility.latestgpspoint.service.LatestGpsPointService;
import backend.capstone.global.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DayRouteFacade {

    private final DayRouteService dayRouteService;
    private final GpsPointService gpsPointService;
    private final LatestGpsPointService latestGpsPointService;
    private final CareLocationSseEventService careLocationSseEventService;
    private final VisitedRegionService visitedRegionService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Retryable(
        retryFor = {
            CannotAcquireLockException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public GpsPointBatchUploadResponse uploadGpsPoint(LocalDate date, Long userId,
        GpsPointBatchUploadRequest request) {
        DayRoute dayRoute = dayRouteService.getOrCreate(userId, date);

        if (!request.gpsPoints().isEmpty()) {
            log.info("GPS 좌표 업로드 수신 userId={} dayRouteId={} uploadTrigger={} pointCount={} distanceKm={}",
                userId, dayRoute.getId(), request.uploadTrigger(), request.gpsPoints().size(),
                request.distance());
            gpsPointService.batchInsert(dayRoute.getId(), request);
            careLocationSseEventService.publishLocationUpdated(
                latestGpsPointService.upsertLatestLocation(userId, request.gpsPoints()));
            dayRouteService.markHasGpsPoints(dayRoute);
        }

        dayRouteService.updateDistance(dayRoute, request.distance());

        //분석 이벤트 발행
        if (!request.gpsPoints().isEmpty()) {
            applicationEventPublisher.publishEvent(new GpsPointsUploadedEvent(dayRoute.getId()));
        }

        return new GpsPointBatchUploadResponse("좌표 업로드에 성공했습니다.");
    }

    @Transactional
    public DayRouteDetailResponse getDayRouteDetail(LocalDate date, Long userId) {
        DayRoute dayRoute = dayRouteService.getDayRouteByDateAndUserId(date, userId);

//        if (dayRoute.isHasPolyline() && dayRoute.getEncodedPath() == null) {
//            List<GpsPoint> gpsPoints = gpsPointService.getGpsPointsByDayRouteId(dayRoute);
//            String encodePath = PolylineUtil.encode(gpsPoints);
//            dayRoute.updateEncodedPath(encodePath, gpsPoints.size());
//        }

        List<GpsPoint> gpsPoints = gpsPointService.getGpsPointsByDayRouteId(dayRoute);
        return DayRouteMapper.toDayRouteDetailResponse(dayRoute, gpsPoints);
    }

    @Transactional(readOnly = true)
    public DayRouteSummaryResponse getDayRouteSummary(LocalDate date, Long userId) {
        DayRoute dayRoute = dayRouteService.getDayRouteByDateAndUserId(date, userId);
        List<String> visitedDongName = visitedRegionService.getVisitedRegionDongNames(dayRoute);
        return DayRouteMapper.toDayRouteSummaryResponse(dayRoute, visitedDongName);
    }

    @Transactional(readOnly = true)
    public DayRouteMonthlyResponse getDayRoutesByMonth(int year, int month, Long userId) {
        List<DayRoute> dayRoutes = dayRouteService.getDayRoutesByMonth(userId, year, month);
        return DayRouteMapper.toDayRouteMonthlyResponse(year, month, dayRoutes);
    }

    @Transactional(readOnly = true)
    public DayRouteBookmarkListResponse getBookmarkedDayRoutes(Long userId, LocalDate cursorDate,
        int size) {
        List<DayRoute> fetchedDayRoutes = dayRouteService.getBookmarkedDayRoutes(
            userId, cursorDate, size + 1);
        boolean hasNext = fetchedDayRoutes.size() > size;
        List<DayRoute> dayRoutes = hasNext ? fetchedDayRoutes.subList(0, size) : fetchedDayRoutes;
        LocalDate nextCursorDate = hasNext ? dayRoutes.get(dayRoutes.size() - 1).getDate() : null;

        return DayRouteMapper.toDayRouteBookmarkListResponse(dayRoutes, hasNext, nextCursorDate,
            visitedRegionService.getVisitedRegionDongNames(dayRoutes));
    }

    @Transactional
    public DayRouteMemoResponse replaceMemo(LocalDate date, Long userId,
        DayRouteMemoRequest request) {
        DayRoute dayRoute = dayRouteService.getOrCreate(userId, date);
        dayRouteService.replaceMemo(dayRoute, request.memo());

        return new DayRouteMemoResponse(dayRoute.getMemo());
    }

    @Transactional
    public DayRouteTitleResponse replaceTitle(LocalDate date, Long userId,
        DayRouteTitleRequest request) {
        DayRoute dayRoute = dayRouteService.getOrCreate(userId, date);
        dayRouteService.replaceTitle(dayRoute, request.title());

        return new DayRouteTitleResponse(dayRoute.getTitle());
    }

    @Transactional
    public DayRouteBookmarkResponse toggleBookmark(LocalDate date, Long userId) {
        DayRoute dayRoute = dayRouteService.getOrCreate(userId, date);
        boolean isBookmarked = dayRouteService.toggleBookmark(dayRoute);

        return new DayRouteBookmarkResponse(isBookmarked);
    }

    @Transactional
    public void bookmarkDayRoutes(Long userId, DayRouteBookmarkBatchRequest request) {
        request.dates().stream()
            .distinct()
            .map(date -> dayRouteService.getOrCreate(userId, date))
            .forEach(dayRouteService::toggleBookmark);
    }

    @Recover
    public GpsPointBatchUploadResponse recover(RuntimeException e, Long userId,
        GpsPointBatchUploadRequest request) {
        throw new BusinessException(DayRouteErrorCode.GPS_POINT_UPLOAD_FAILURE);
    }
}
