package backend.capstone.domain.dayroute.service;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.exception.DayRouteErrorCode;
import backend.capstone.domain.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.place.repository.PlaceRepository;
import backend.capstone.domain.user.service.UserService;
import backend.capstone.global.exception.BusinessException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DayRouteService {

    private final DayRouteRepository dayRouteRepository;
    private final PlaceRepository placeRepository;
    private final UserService userService;

    @Transactional
    public DayRoute getDayRouteByDateAndUserId(LocalDate date, Long userId) {
        return dayRouteRepository.findByUserIdAndDate(userId, date)
            .orElseThrow(() -> new BusinessException(DayRouteErrorCode.DAY_ROUTE_NOT_FOUND));
    }

    @Transactional
    public DayRoute getOrCreate(Long userId, LocalDate date) {
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

    @Transactional
    public void updateTitle(DayRoute dayRoute, String title) {
        dayRoute.updateTitle(title);
        refreshHasManualData(dayRoute);
    }

    @Transactional
    public void updateMemo(DayRoute dayRoute, String memo) {
        dayRoute.updateMemo(memo);
        refreshHasManualData(dayRoute);
    }

    @Transactional
    public boolean toggleBookmark(DayRoute dayRoute) {
        return dayRoute.toggleBookmarked();
    }

    @Transactional
    public void updateTime(DayRoute dayRoute, LocalDateTime startTime, LocalDateTime endTime) {
        dayRoute.updateTime(startTime, endTime);
    }

    @Transactional
    public void updateDistance(DayRoute dayRoute, double distance) {
        dayRoute.updateDistance(distance);
    }

    @Transactional
    public void markHasGpsPoints(DayRoute dayRoute) {
        dayRoute.markHasGpsPoints();
    }

    @Transactional
    public void refreshHasManualData(DayRoute dayRoute) {
        dayRoute.updateHasManualData(hasManualData(dayRoute));
    }

    private boolean hasManualData(DayRoute dayRoute) {
        return hasText(dayRoute.getTitle())
            || hasText(dayRoute.getMemo())
            || placeRepository.existsByDayRoute(dayRoute);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
