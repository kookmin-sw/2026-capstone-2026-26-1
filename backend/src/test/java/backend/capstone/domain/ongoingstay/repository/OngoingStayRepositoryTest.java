package backend.capstone.domain.ongoingstay.repository;

import static org.assertj.core.api.Assertions.assertThat;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.repository.GpsPointRepository;
import backend.capstone.domain.ongoingstay.entity.OngoingStay;
import backend.capstone.domain.user.entity.ProviderType;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OngoingStayRepositoryTest {

    @Autowired
    private OngoingStayRepository ongoingStayRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DayRouteRepository dayRouteRepository;

    @Autowired
    private GpsPointRepository gpsPointRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 종료시각이_지난_dayRoute의_ongoingStay만_조회한다() {
        User user = saveUser();
        DayRoute endedDayRoute = saveDayRoute(user, LocalDate.of(2026, 5, 1));
        DayRoute activeDayRoute = saveDayRoute(user, LocalDate.of(2026, 5, 3));

        insertOngoingStay(endedDayRoute, Instant.parse("2026-05-01T00:00:00Z"));
        insertOngoingStay(activeDayRoute, Instant.parse("2026-05-03T00:00:00Z"));

        List<Long> dayRouteIds = ongoingStayRepository.findDayRouteIdsWithEndedOngoingStay(
            Instant.parse("2026-05-03T12:00:00Z"));

        assertThat(dayRouteIds).containsExactly(endedDayRoute.getId());
    }

    private User saveUser() {
        return userRepository.save(User.builder()
            .provider(ProviderType.KAKAO)
            .providerId(UUID.randomUUID().toString())
            .nickname("tester")
            .profileImageUrl("https://example.com/profile.png")
            .build());
    }

    private DayRoute saveDayRoute(User user, LocalDate date) {
        return dayRouteRepository.save(DayRoute.builder()
            .user(user)
            .date(date)
            .build());
    }

    private void insertOngoingStay(DayRoute dayRoute, Instant recordedAt) {
        jdbcTemplate.update(
            "insert into gps_point(day_route_id, latitude, longitude, recorded_at) values (?, ?, ?, ?)",
            dayRoute.getId(),
            37.55582,
            126.90551,
            recordedAt
        );

        GpsPoint gpsPoint = gpsPointRepository.findByDayRouteIdOrderByRecordedAt(dayRoute).getFirst();
        OngoingStay ongoingStay = OngoingStay.start(dayRoute, gpsPoint);
        ongoingStayRepository.save(ongoingStay);
    }
}
