package backend.capstone.domain.dayroute.service;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.exception.DayRouteErrorCode;
import backend.capstone.domain.dayroute.mapper.DayRouteMapper;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.user.service.UserService;
import backend.capstone.global.exception.BusinessException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DayRouteService {

    private final DayRouteRepository dayRouteRepository;
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

}
