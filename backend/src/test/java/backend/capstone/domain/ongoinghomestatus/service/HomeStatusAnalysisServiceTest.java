package backend.capstone.domain.ongoinghomestatus.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.bookmarkplace.repository.BookmarkPlaceRepository;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.entity.DayRouteHomeStatus;
import backend.capstone.domain.dayroute.repository.DayRouteRepository;
import backend.capstone.domain.ongoinghomestatus.entity.HomeZoneStatus;
import backend.capstone.domain.ongoinghomestatus.entity.OngoingHomeStatus;
import backend.capstone.domain.ongoinghomestatus.repository.OngoingHomeStatusRepository;
import backend.capstone.domain.user.entity.ProviderType;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
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
class HomeStatusAnalysisServiceTest {

    private static final double HOME_LATITUDE = 37.5665;
    private static final double HOME_LONGITUDE = 126.9780;
    private static final double INSIDE_HOME_LATITUDE = 37.56655;
    private static final double INSIDE_HOME_LONGITUDE = 126.97805;
    private static final double OUTSIDE_HOME_LATITUDE = 37.56780;
    private static final double OUTSIDE_HOME_LONGITUDE = 126.9780;

    @Autowired
    private HomeStatusAnalysisService homeStatusAnalysisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DayRouteRepository dayRouteRepository;

    @Autowired
    private BookmarkPlaceRepository bookmarkPlaceRepository;

    @Autowired
    private OngoingHomeStatusRepository ongoingHomeStatusRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 집_북마크가_없으면_NO_HOME_BOOKMARK로_처리된다() {
        User user = saveUser();
        DayRoute dayRoute = saveDayRoute(user, LocalDate.of(2026, 5, 1));
        insertGpsPoint(dayRoute, INSIDE_HOME_LATITUDE, INSIDE_HOME_LONGITUDE,
            Instant.parse("2026-05-01T00:00:00Z"));

        homeStatusAnalysisService.analyzeHomeStatus(dayRoute.getId());

        DayRoute savedDayRoute = dayRouteRepository.findById(dayRoute.getId()).orElseThrow();

        assertThat(savedDayRoute.getDayRouteHomeStatus()).isEqualTo(
            DayRouteHomeStatus.NO_HOME_BOOKMARK);
        assertThat(ongoingHomeStatusRepository.findByDayRoute(savedDayRoute)).isEmpty();
    }

    @Test
    void 첫_GPS가_집_안이면_AT_HOME으로_초기화된다() {
        User user = saveUser();
        DayRoute dayRoute = saveDayRoute(user, LocalDate.of(2026, 5, 1));
        saveHomeBookmark(user);
        Instant firstPointAt = Instant.parse("2026-05-01T00:00:00Z");
        insertGpsPoint(dayRoute, INSIDE_HOME_LATITUDE, INSIDE_HOME_LONGITUDE, firstPointAt);

        homeStatusAnalysisService.analyzeHomeStatus(dayRoute.getId());

        DayRoute savedDayRoute = dayRouteRepository.findById(dayRoute.getId()).orElseThrow();
        OngoingHomeStatus ongoingHomeStatus = ongoingHomeStatusRepository.findByDayRoute(
                savedDayRoute)
            .orElseThrow();

        assertThat(savedDayRoute.getDayRouteHomeStatus()).isEqualTo(DayRouteHomeStatus.AT_HOME);
        assertThat(savedDayRoute.getExitHomeTime()).isNull();
        assertThat(savedDayRoute.getEnterHomeTime()).isNull();
        assertThat(savedDayRoute.getHomeAnalysisLastPointAt()).isEqualTo(firstPointAt);
        assertThat(ongoingHomeStatus.getCurrentZoneStatus()).isEqualTo(HomeZoneStatus.IN_HOME);
        assertThat(ongoingHomeStatus.getCandidateZoneStatus()).isNull();
    }

    @Test
    void 첫_GPS가_집_밖이면_OUTING이지만_exitHomeTime은_기록하지_않는다() {
        User user = saveUser();
        DayRoute dayRoute = saveDayRoute(user, LocalDate.of(2026, 5, 1));
        saveHomeBookmark(user);
        Instant firstPointAt = Instant.parse("2026-05-01T00:00:00Z");
        insertGpsPoint(dayRoute, OUTSIDE_HOME_LATITUDE, OUTSIDE_HOME_LONGITUDE, firstPointAt);

        homeStatusAnalysisService.analyzeHomeStatus(dayRoute.getId());

        DayRoute savedDayRoute = dayRouteRepository.findById(dayRoute.getId()).orElseThrow();
        OngoingHomeStatus ongoingHomeStatus = ongoingHomeStatusRepository.findByDayRoute(
                savedDayRoute)
            .orElseThrow();

        assertThat(savedDayRoute.getDayRouteHomeStatus()).isEqualTo(DayRouteHomeStatus.OUTING);
        assertThat(savedDayRoute.getExitHomeTime()).isNull();
        assertThat(savedDayRoute.getEnterHomeTime()).isNull();
        assertThat(ongoingHomeStatus.getCurrentZoneStatus()).isEqualTo(HomeZoneStatus.OUT_HOME);
    }

    @Test
    void 집_안에서_시작한_후_집_밖_상태가_3분_이상_유지되면_OUTING으로_확정된다() {
        User user = saveUser();
        DayRoute dayRoute = saveDayRoute(user, LocalDate.of(2026, 5, 1));
        saveHomeBookmark(user);

        Instant startAt = Instant.parse("2026-05-01T00:00:00Z");
        Instant candidateStartAt = Instant.parse("2026-05-01T00:03:00Z");
        Instant transitionObservedAt = Instant.parse("2026-05-01T00:06:00Z");

        insertGpsPoint(dayRoute, INSIDE_HOME_LATITUDE, INSIDE_HOME_LONGITUDE, startAt);
        insertGpsPoint(dayRoute, OUTSIDE_HOME_LATITUDE, OUTSIDE_HOME_LONGITUDE, candidateStartAt);
        insertGpsPoint(dayRoute, OUTSIDE_HOME_LATITUDE, OUTSIDE_HOME_LONGITUDE,
            transitionObservedAt);

        homeStatusAnalysisService.analyzeHomeStatus(dayRoute.getId());

        DayRoute savedDayRoute = dayRouteRepository.findById(dayRoute.getId()).orElseThrow();
        OngoingHomeStatus ongoingHomeStatus = ongoingHomeStatusRepository.findByDayRoute(
                savedDayRoute)
            .orElseThrow();

        assertThat(savedDayRoute.getDayRouteHomeStatus()).isEqualTo(DayRouteHomeStatus.OUTING);
        assertThat(savedDayRoute.getExitHomeTime()).isEqualTo(candidateStartAt);
        assertThat(savedDayRoute.getHomeAnalysisLastPointAt()).isEqualTo(transitionObservedAt);
        assertThat(ongoingHomeStatus.getCurrentZoneStatus()).isEqualTo(HomeZoneStatus.OUT_HOME);
        assertThat(ongoingHomeStatus.getCandidateZoneStatus()).isNull();
    }

    @Test
    void 집_밖_candidate가_생겼다가_다시_집_안이면_외출로_확정되지_않는다() {
        User user = saveUser();
        DayRoute dayRoute = saveDayRoute(user, LocalDate.of(2026, 5, 1));
        saveHomeBookmark(user);

        insertGpsPoint(dayRoute, INSIDE_HOME_LATITUDE, INSIDE_HOME_LONGITUDE,
            Instant.parse("2026-05-01T00:00:00Z"));
        insertGpsPoint(dayRoute, OUTSIDE_HOME_LATITUDE, OUTSIDE_HOME_LONGITUDE,
            Instant.parse("2026-05-01T00:03:00Z"));
        insertGpsPoint(dayRoute, INSIDE_HOME_LATITUDE, INSIDE_HOME_LONGITUDE,
            Instant.parse("2026-05-01T00:04:00Z"));

        homeStatusAnalysisService.analyzeHomeStatus(dayRoute.getId());

        DayRoute savedDayRoute = dayRouteRepository.findById(dayRoute.getId()).orElseThrow();
        OngoingHomeStatus ongoingHomeStatus = ongoingHomeStatusRepository.findByDayRoute(
                savedDayRoute)
            .orElseThrow();

        assertThat(savedDayRoute.getDayRouteHomeStatus()).isEqualTo(DayRouteHomeStatus.AT_HOME);
        assertThat(savedDayRoute.getExitHomeTime()).isNull();
        assertThat(ongoingHomeStatus.getCurrentZoneStatus()).isEqualTo(HomeZoneStatus.IN_HOME);
        assertThat(ongoingHomeStatus.getCandidateZoneStatus()).isNull();
        assertThat(ongoingHomeStatus.getCandidateStartedAt()).isNull();
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

    private BookmarkPlace saveHomeBookmark(User user) {
        return bookmarkPlaceRepository.save(BookmarkPlace.builder()
            .user(user)
            .type(BookmarkPlaceType.HOME)
            .name("home")
            .roadAddress("seoul")
            .latitude(HOME_LATITUDE)
            .longitude(HOME_LONGITUDE)
            .build());
    }

    private void insertGpsPoint(DayRoute dayRoute, double latitude, double longitude,
        Instant recordedAt) {
        jdbcTemplate.update(
            "insert into gps_point(day_route_id, latitude, longitude, recorded_at) values (?, ?, ?, ?)",
            dayRoute.getId(),
            latitude,
            longitude,
            recordedAt
        );
    }
}
