package backend.capstone.domain.ongoinghomestatus.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.ongoinghomestatus.entity.HomeZoneStatus;
import backend.capstone.domain.ongoinghomestatus.entity.OngoingHomeStatus;
import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OutingDurationAccumulatorTest {

    private OutingDurationAccumulator outingDurationAccumulator;

    @BeforeEach
    void setUp() {
        outingDurationAccumulator = new OutingDurationAccumulator();
    }

    @Test
    void previousPointAt이_null이면_외출시간을_누적하지_않는다() throws Exception {
        OngoingHomeStatus ongoingHomeStatus = createOngoingHomeStatus(HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:00:00Z"));

        long outingSeconds = outingDurationAccumulator.calculateSegmentOutingDurationSeconds(ongoingHomeStatus, HomeZoneStatus.OUT_HOME, null, Instant.parse("2026-05-01T00:03:00Z"));

        assertThat(outingSeconds).isZero();
    }

    @Test
    void 현재_확정상태가_OUT_HOME이_아니면_외출시간을_누적하지_않는다() throws Exception {
        OngoingHomeStatus ongoingHomeStatus = createOngoingHomeStatus(HomeZoneStatus.IN_HOME, Instant.parse("2026-05-01T00:00:00Z"));

        long outingSeconds = outingDurationAccumulator.calculateSegmentOutingDurationSeconds(ongoingHomeStatus, HomeZoneStatus.IN_HOME, Instant.parse("2026-05-01T00:00:00Z"), Instant.parse("2026-05-01T00:03:00Z"));

        assertThat(outingSeconds).isZero();
    }

    @Test
    void 현재_확정상태가_OUT_HOME이고_candidate가_없으면_직전_구간을_누적한다() throws Exception {
        OngoingHomeStatus ongoingHomeStatus = createOngoingHomeStatus(HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:00:00Z"));

        long outingSeconds = outingDurationAccumulator.calculateSegmentOutingDurationSeconds(ongoingHomeStatus, HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:00:00Z"), Instant.parse("2026-05-01T00:03:00Z"));

        assertThat(outingSeconds).isEqualTo(180);
    }

    @Test
    void 귀가_candidate가_생기고_이번_관측도_귀가방향이면_구간을_누적하지_않는다() throws Exception {
        OngoingHomeStatus ongoingHomeStatus = createOngoingHomeStatus(HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:00:00Z"));
        ongoingHomeStatus.startCandidate(HomeZoneStatus.IN_HOME, Instant.parse("2026-05-01T00:03:00Z"));

        long outingSeconds = outingDurationAccumulator.calculateSegmentOutingDurationSeconds(ongoingHomeStatus, HomeZoneStatus.IN_HOME, Instant.parse("2026-05-01T00:03:00Z"), Instant.parse("2026-05-01T00:06:00Z"));

        assertThat(outingSeconds).isZero();
    }

    @Test
    void 귀가_candidate가_취소되고_다시_OUT_HOME이_관측되면_candidate_시작시점부터_복구누적한다() throws Exception {
        OngoingHomeStatus ongoingHomeStatus = createOngoingHomeStatus(HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:00:00Z"));
        ongoingHomeStatus.startCandidate(HomeZoneStatus.IN_HOME, Instant.parse("2026-05-01T00:03:00Z"));

        long outingSeconds = outingDurationAccumulator.calculateSegmentOutingDurationSeconds(ongoingHomeStatus, HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:03:00Z"), Instant.parse("2026-05-01T00:06:00Z"));

        assertThat(outingSeconds).isEqualTo(180);
    }

    @Test
    void 외출전이가_확정되면_candidate_시작시점부터_현재시점까지를_보정누적한다() {
        long outingSeconds = outingDurationAccumulator.calculateConfirmedOutingDurationSeconds(HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:03:00Z"), Instant.parse("2026-05-01T00:06:00Z"));

        assertThat(outingSeconds).isEqualTo(180);
    }

    @Test
    void 귀가전이가_확정되면_보정누적하지_않는다() {
        long outingSeconds = outingDurationAccumulator.calculateConfirmedOutingDurationSeconds(HomeZoneStatus.IN_HOME, Instant.parse("2026-05-01T00:03:00Z"), Instant.parse("2026-05-01T00:06:00Z"));

        assertThat(outingSeconds).isZero();
    }

    private OngoingHomeStatus createOngoingHomeStatus(HomeZoneStatus currentZoneStatus, Instant recordedAt) throws Exception {
        DayRoute dayRoute = DayRoute.builder()
            .date(LocalDate.of(2026, 5, 1))
            .build();
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
