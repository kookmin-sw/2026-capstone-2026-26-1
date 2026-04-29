package backend.capstone.domain.dayroute.service;

import backend.capstone.domain.dayroute.entity.AnalysisStatus;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.exception.DayRouteErrorCode;
import backend.capstone.domain.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.place.repository.PlaceRepository;
import backend.capstone.domain.user.service.UserService;
import backend.capstone.global.exception.BusinessException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
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

    @Transactional(readOnly = true)
    public DayRoute getDayRouteById(Long dayRouteId) {
        return dayRouteRepository.findById(dayRouteId)
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

    @Transactional(readOnly = true)
    public LocalDateTime getDayRouteEndTime(DayRoute dayRoute) {
        //TODO: fetch join 적용
        LocalDate routeDate = dayRoute.getDate();
        LocalTime dayStartTime = dayRoute.getUser().getDayStartTime();
        LocalTime dayEndTime = dayRoute.getUser().getDayEndTime();

        LocalDate endDate = routeDate;
        if (dayEndTime.isBefore(dayStartTime) || dayEndTime.equals(dayStartTime)) {
            endDate = routeDate.plusDays(1);
        }
        return LocalDateTime.of(endDate, dayEndTime);
    }

    @Transactional(readOnly = true)
    public List<DayRoute> getDayRoutesByMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return dayRouteRepository.findByUserIdAndDateBetweenOrderByDate(userId,
            yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    @Transactional
    public void replaceTitle(DayRoute dayRoute, String title) {
        dayRoute.updateTitle(normalizeNullableText(title));
        refreshHasManualData(dayRoute);
    }

    @Transactional
    public void replaceMemo(DayRoute dayRoute, String memo) {
        dayRoute.updateMemo(normalizeNullableText(memo));
        refreshHasManualData(dayRoute);
    }

    @Transactional
    public boolean toggleBookmark(DayRoute dayRoute) {
        return dayRoute.toggleBookmarked();
    }

    @Transactional
    public void updateTime(DayRoute dayRoute, Instant startTime, Instant endTime) {
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

    @Transactional(readOnly = true)
    public List<DayRoute> getStayAnalysisTargetDayRoute(AnalysisStatus status) {
        return dayRouteRepository.findStayAnalysisTargets(status);
    }

    private boolean hasManualData(DayRoute dayRoute) {
        return hasText(dayRoute.getTitle())
            || hasText(dayRoute.getMemo())
            || placeRepository.existsByDayRoute(dayRoute);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeNullableText(String value) {
        return hasText(value) ? value : null;
    }
}
