package backend.capstone.domain.dayroute.service;

import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.exception.DayRouteErrorCode;
import backend.capstone.domain.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.gpspoint.dto.GpsPointRecordedAtRange;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.user.entity.User;
import backend.capstone.global.exception.BusinessException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DayRouteService {

    private final DayRouteRepository dayRouteRepository;
    private final GpsPointService gpsPointService;

    //TODO: 업로드 실패 예외처리 필요
    @Transactional
    public GpsPointBatchUploadResponse uploadGpsPoint(User user,
        GpsPointBatchUploadRequest request) {
        DayRoute dayRoute = createDayRouteIfNotExists(user, request.date());
        gpsPointService.batchInsert(dayRoute.getId(), request);

        // 업로드된 좌표의 시간 범위로 DayRoute 시간 업데이트
        GpsPointRecordedAtRange gpsPointRange = gpsPointService.getGpsPointRange(dayRoute);
        dayRoute.updateTime(gpsPointRange.getStartTime(), gpsPointRange.getEndTime());

        return new GpsPointBatchUploadResponse("좌표 업로드에 성공했습니다.");
    }

    @Transactional(readOnly = true)
    public DayRouteDetailResponse getDayRouteDetail(Long dayRouteId, User user) {
        DayRoute dayRoute = dayRouteRepository.findByIdAndUserWithGpsPoints(dayRouteId, user)
            .orElseThrow(() -> new BusinessException(DayRouteErrorCode.CANNOT_ACCESS_DAY_ROUTE));

        return DayRouteMapper.toDayRouteDetailResponse(dayRoute);
    }

    private DayRoute createDayRouteIfNotExists(User user, LocalDate date) {
        return dayRouteRepository.findByUserAndDate(user, date)
            .orElseGet(() -> {
                DayRoute newDayRoute = DayRouteMapper.toEntity(user, date);
                return dayRouteRepository.save(newDayRoute);
            });
    }

}
