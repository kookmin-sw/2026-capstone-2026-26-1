package backend.capstone.domain.mobility.analysis.ongoinghomestatus.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.capstone.domain.mobility.analysis.ongoinghomestatus.entity.HomeZoneStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OutingDurationAccumulatorTest {

    private OutingDurationAccumulator outingDurationAccumulator;

    @BeforeEach
    void setUp() {
        outingDurationAccumulator = new OutingDurationAccumulator();
    }

    @Test
    void previousPointAt이_null이면_외출시간을_누적하지_않는다() {
        long outingSeconds = outingDurationAccumulator.calculateOutingDurationSeconds(
            HomeZoneStatus.OUT_HOME, null, Instant.parse("2026-05-01T00:03:00Z"));

        assertThat(outingSeconds).isZero();
    }

    @Test
    void 현재_확정상태가_OUT_HOME이_아니면_외출시간을_누적하지_않는다() {
        long outingSeconds = outingDurationAccumulator.calculateOutingDurationSeconds(
            HomeZoneStatus.IN_HOME, Instant.parse("2026-05-01T00:00:00Z"),
            Instant.parse("2026-05-01T00:03:00Z"));

        assertThat(outingSeconds).isZero();
    }

    @Test
    void 현재_확정상태가_OUT_HOME이면_직전_포인트부터_이번_포인트까지를_누적한다() {
        long outingSeconds = outingDurationAccumulator.calculateOutingDurationSeconds(
            HomeZoneStatus.OUT_HOME, Instant.parse("2026-05-01T00:00:00Z"),
            Instant.parse("2026-05-01T00:03:00Z"));

        assertThat(outingSeconds).isEqualTo(180);
    }
}
