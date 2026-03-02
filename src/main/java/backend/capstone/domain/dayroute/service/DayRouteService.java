package backend.capstone.domain.dayroute.service;

import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.exception.DayRouteErrorCode;
import backend.capstone.domain.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.gpspoint.dto.GpsPointRecordedAtRange;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.user.entity.User;
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

    @Retryable(
        retryFor = {
            CannotAcquireLockException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public GpsPointBatchUploadResponse uploadGpsPoint(Long userId,
        GpsPointBatchUploadRequest request) {
        User user = userService.findById(userId);
        DayRoute dayRoute = createDayRouteIfNotExists(user, request.date());
        gpsPointService.batchInsert(dayRoute.getId(), request);

        // 업로드된 좌표의 시간 범위로 DayRoute 시간 업데이트
        GpsPointRecordedAtRange gpsPointRange = gpsPointService.getGpsPointRange(dayRoute);
        dayRoute.updateTime(gpsPointRange.getStartTime(), gpsPointRange.getEndTime());

        return new GpsPointBatchUploadResponse("좌표 업로드에 성공했습니다.");
    }

    @Transactional(readOnly = true)
    public DayRouteDetailResponse getDayRouteDetail(Long dayRouteId, Long userId) {
        DayRoute dayRoute = dayRouteRepository.findByIdAndUser(dayRouteId, userId)
            .orElseThrow(() -> new BusinessException(DayRouteErrorCode.CANNOT_ACCESS_DAY_ROUTE));

        List<GpsPoint> gpsPoints = gpsPointService.getGpsPointsByDayRouteId(dayRouteId);

        return DayRouteMapper.toDayRouteDetailResponse(dayRoute, gpsPoints);
    }

    private DayRoute createDayRouteIfNotExists(User user, LocalDate date) {
        return dayRouteRepository.findByUserAndDate(user, date)
            .orElseGet(() -> {
                try {
                    return dayRouteRepository.save(DayRouteMapper.toEntity(user, date));
                } catch (DataIntegrityViolationException e) {
                    // 다른 트랜잭션이 방금 만들어서 uk_user_date에 걸린 케이스
                    return dayRouteRepository.findByUserAndDate(user, date)
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
