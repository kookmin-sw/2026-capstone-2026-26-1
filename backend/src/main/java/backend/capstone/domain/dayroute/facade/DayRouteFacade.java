package backend.capstone.domain.dayroute.facade;

import backend.capstone.domain.dayroute.dto.DayRouteBookmarkResponse;
import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMemoRequest;
import backend.capstone.domain.dayroute.dto.DayRouteMemoResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMonthlyResponse;
import backend.capstone.domain.dayroute.dto.DayRouteTitleRequest;
import backend.capstone.domain.dayroute.dto.DayRouteTitleResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.exception.DayRouteErrorCode;
import backend.capstone.domain.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.gpspoint.dto.GpsPointRecordedAtRange;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.global.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DayRouteFacade {

    private final DayRouteService dayRouteService;
    private final GpsPointService gpsPointService;

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
            gpsPointService.batchInsert(dayRoute.getId(), request);
            dayRouteService.markHasGpsPoints(dayRoute);

            // 업로드된 좌표의 시간 범위로 DayRoute 시간 업데이트
            GpsPointRecordedAtRange gpsPointRange = gpsPointService.getGpsPointRange(dayRoute);
            dayRouteService.updateTime(dayRoute, gpsPointRange.startTime(),
                gpsPointRange.endTime());
        }

        // 이동거리 업데이트
        dayRouteService.updateDistance(dayRoute, request.distance());
        dayRoute.markAnalysisNeeded();

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
    public DayRouteMonthlyResponse getDayRoutesByMonth(int year, int month, Long userId) {
        List<DayRoute> dayRoutes = dayRouteService.getDayRoutesByMonth(userId, year, month);
        return DayRouteMapper.toDayRouteMonthlyResponse(year, month, dayRoutes);
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

    @Recover
    public GpsPointBatchUploadResponse recover(RuntimeException e, Long userId,
        GpsPointBatchUploadRequest request) {
        // 예: 도메인 예외로 변환
        throw new BusinessException(DayRouteErrorCode.GPS_POINT_UPLOAD_FAILURE);
    }
}
