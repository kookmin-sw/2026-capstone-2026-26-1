package backend.capstone.domain.mobility.analysis.ongoinghomestatus.service;

import backend.capstone.domain.mobility.analysis.ongoinghomestatus.entity.HomeZoneStatus;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OutingDurationAccumulator {

    public long calculateOutingDurationSeconds(HomeZoneStatus currentZoneStatus,
        Instant previousPointAt, Instant currentPointAt) {
        if (previousPointAt == null || currentZoneStatus != HomeZoneStatus.OUT_HOME) {
            return 0;
        }

        return Duration.between(previousPointAt, currentPointAt).getSeconds();
    }
}
