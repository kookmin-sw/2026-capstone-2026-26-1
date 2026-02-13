package backend.capstone.domain.dayroute.service;

import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.gpspoint.service.GpsPointService;
import backend.capstone.domain.user.entity.User;
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

        return new GpsPointBatchUploadResponse("좌표 업로드에 성공했습니다.");
    }

    private DayRoute createDayRouteIfNotExists(User user, LocalDate date) {
        return dayRouteRepository.findByUserAndDate(user, date)
            .orElseGet(() -> {
                DayRoute newDayRoute = DayRouteMapper.toEntity(user, date);
                return dayRouteRepository.save(newDayRoute);
            });
    }

}
