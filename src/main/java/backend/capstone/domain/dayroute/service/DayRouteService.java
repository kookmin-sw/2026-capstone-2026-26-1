package backend.capstone.domain.dayroute.service;

import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.dto.GpsPointsResponse;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.exception.DayRouteErrorCode;
import backend.capstone.domain.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.gpspoint.dto.GpsPointRecordedAtRange;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceUpdateRequest;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import backend.capstone.domain.place.entity.Place;
import backend.capstone.domain.place.service.PlaceService;
import backend.capstone.domain.user.service.UserService;
import backend.capstone.global.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DayRouteService {

    private final DayRouteRepository dayRouteRepository;
    private final GpsPointService gpsPointService;
    private final UserService userService;
    private final PlaceService placeService;

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
        DayRoute dayRoute = createDayRouteIfNotExists(userId, date);
        gpsPointService.batchInsert(dayRoute.getId(), request);

        // 업로드된 좌표의 시간 범위로 DayRoute 시간 업데이트
        GpsPointRecordedAtRange gpsPointRange = gpsPointService.getGpsPointRange(dayRoute);
        dayRoute.updateTime(gpsPointRange.getStartTime(), gpsPointRange.getEndTime());

        return new GpsPointBatchUploadResponse("좌표 업로드에 성공했습니다.");
    }

    @Transactional(readOnly = true)
    public GpsPointsResponse getGpsPoints(LocalDate date, Long userId) {
        DayRoute dayRoute = getDayRouteByDateAndUserId(date, userId);
        List<GpsPoint> gpsPoints = gpsPointService.getGpsPointsByDayRouteId(dayRoute);

        return DayRouteMapper.toGpsPointsResponse(gpsPoints);
    }

    @Transactional(readOnly = true)
    public DayRouteDetailResponse getDayRouteDetail(LocalDate date, Long userId) {
        DayRoute dayRoute = getDayRouteByDateAndUserId(date, userId);
        List<Place> places = placeService.getPlacesByDayRoute(dayRoute);

        return DayRouteMapper.toDayRouteDetailResponse(dayRoute, places);
    }

    @Transactional
    public PlaceAddResponse addPlaceToDayRoute(LocalDate date, Long userId,
        PlaceAddRequest request) {
        DayRoute dayRoute = createDayRouteIfNotExists(userId, date);

        return placeService.addPlace(dayRoute, request);
    }

    @Transactional
    public PlaceUpdateResponse updatePlace(LocalDate date, Long userId,
        Long placeId, PlaceUpdateRequest request) {
        DayRoute dayRoute = getDayRouteByDateAndUserId(date, userId);

        return placeService.updatePlace(dayRoute, placeId, request);
    }

    private DayRoute getDayRouteByDateAndUserId(LocalDate date, Long userId) {
        return dayRouteRepository.findByUserIdAndDate(userId, date)
            .orElseThrow(() -> new BusinessException(DayRouteErrorCode.DAY_ROUTE_NOT_FOUND));
    }

    private DayRoute createDayRouteIfNotExists(Long userId, LocalDate date) {
        return dayRouteRepository.findByUserIdAndDate(userId, date)
            .orElseGet(() -> {
                try {
                    return dayRouteRepository.save(
                        DayRouteMapper.toEntity(userService.findById(userId), date));
                } catch (DataIntegrityViolationException e) {
                    // 다른 트랜잭션이 방금 만들어서 uk_user_date에 걸린 케이스
                    return dayRouteRepository.findByUserIdAndDate(userId, date)
                        .orElseThrow(
                            () -> new BusinessException(DayRouteErrorCode.DAY_ROUTE_CREATE_FAILED));
                }
            });
    }

    @Recover
    public GpsPointBatchUploadResponse recover(RuntimeException e, Long userId,
        GpsPointBatchUploadRequest request) {
        // 예: 도메인 예외로 변환
        throw new BusinessException(DayRouteErrorCode.GPS_POINT_UPLOAD_FAILURE);
    }
}
