package backend.capstone.domain.mobility.analysis.ongoinghomestatus.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.capstone.domain.mobility.analysis.ongoinghomestatus.entity.HomeZoneStatus;
import backend.capstone.domain.mobility.analysis.ongoinghomestatus.entity.OngoingHomeStatus;
import backend.capstone.domain.mobility.dayroute.entity.DayRoute;
import backend.capstone.domain.mobility.dayroute.entity.DayRouteHomeStatus;
import backend.capstone.domain.mobility.gpspoint.entity.GpsPoint;
import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class HomeStatusTransitionHandlerTest {

    private HomeStatusTransitionHandler homeStatusTransitionHandler;

    @BeforeEach
    void setUp() {
        homeStatusTransitionHandler = new HomeStatusTransitionHandler();
    }

    @Test
    void 관측_상태가_현재_상태와_같으면_아무것도_변하지_않는다() throws Exception {
        DayRoute dayRoute = createDayRoute();
        OngoingHomeStatus ongoingHomeStatus = createOngoingHomeStatus(HomeZoneStatus.OUT_HOME,
            Instant.parse("2026-05-01T00:00:00Z"));

        homeStatusTransitionHandler.handleTransition(dayRoute, ongoingHomeStatus,
            HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:05:00Z"));

        assertThat(ongoingHomeStatus.getCurrentZoneStatus()).isEqualTo(HomeZoneStatus.OUT_HOME);
        assertThat(dayRoute.getEnterHomeTime()).isNull();
        assertThat(dayRoute.getOutingTime()).isNull();
    }

    @Test
    void 관측_상태가_다르면_다음_포인트_없이_즉시_귀가로_확정된다() throws Exception {
        DayRoute dayRoute = createDayRoute();
        OngoingHomeStatus ongoingHomeStatus = createOngoingHomeStatus(HomeZoneStatus.OUT_HOME,
            Instant.parse("2026-05-01T00:00:00Z"));
        Instant observedAt = Instant.parse("2026-05-01T00:05:00Z");

        homeStatusTransitionHandler.handleTransition(dayRoute, ongoingHomeStatus,
            HomeZoneStatus.IN_HOME, observedAt);

        assertThat(dayRoute.getDayRouteHomeStatus()).isEqualTo(DayRouteHomeStatus.RETURNED_HOME);
        assertThat(dayRoute.getEnterHomeTime()).isEqualTo(observedAt);
        assertThat(ongoingHomeStatus.getCurrentZoneStatus()).isEqualTo(HomeZoneStatus.IN_HOME);
    }

    @Test
    void 관측_상태가_다르면_다음_포인트_없이_즉시_외출로_확정된다() throws Exception {
        DayRoute dayRoute = createDayRoute();
        OngoingHomeStatus ongoingHomeStatus = createOngoingHomeStatus(HomeZoneStatus.IN_HOME,
            Instant.parse("2026-05-01T00:00:00Z"));
        Instant observedAt = Instant.parse("2026-05-01T00:05:00Z");

        homeStatusTransitionHandler.handleTransition(dayRoute, ongoingHomeStatus,
            HomeZoneStatus.OUT_HOME, observedAt);

        assertThat(dayRoute.getDayRouteHomeStatus()).isEqualTo(DayRouteHomeStatus.OUTING);
        assertThat(dayRoute.getOutingTime()).isEqualTo(observedAt);
        assertThat(ongoingHomeStatus.getCurrentZoneStatus()).isEqualTo(HomeZoneStatus.OUT_HOME);
    }

    private DayRoute createDayRoute() {
        return DayRoute.builder()
            .date(LocalDate.of(2026, 5, 1))
            .build();
    }

    private OngoingHomeStatus createOngoingHomeStatus(HomeZoneStatus currentZoneStatus,
        Instant recordedAt) throws Exception {
        DayRoute dayRoute = createDayRoute();
        GpsPoint gpsPoint = createGpsPoint(recordedAt);
        return OngoingHomeStatus.initialize(dayRoute, gpsPoint, currentZoneStatus);
    }

    private GpsPoint createGpsPoint(Instant recordedAt) throws Exception {
        Constructor<GpsPoint> constructor = GpsPoint.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        GpsPoint gpsPoint = constructor.newInstance();
        ReflectionTestUtils.setField(gpsPoint, "recordedAt", recordedAt);
        return gpsPoint;
    }
}
