package backend.capstone.domain.ongoinghomestatus.service;

import backend.capstone.domain.ongoinghomestatus.entity.HomeZoneStatus;
import backend.capstone.domain.ongoinghomestatus.entity.OngoingHomeStatus;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OutingDurationAccumulator {

    public long calculateSegmentOutingDurationSeconds(OngoingHomeStatus ongoingHomeStatus,
        HomeZoneStatus observedZoneStatus, Instant previousPointAt, Instant currentPointAt) {
        if (previousPointAt == null
            || ongoingHomeStatus.getCurrentZoneStatus() != HomeZoneStatus.OUT_HOME) {
            return 0;
        }

        if (ongoingHomeStatus.getCandidateZoneStatus() == HomeZoneStatus.IN_HOME) {
            if (observedZoneStatus == HomeZoneStatus.OUT_HOME) {
                return Duration.between(ongoingHomeStatus.getCandidateStartedAt(), currentPointAt)
                    .getSeconds();
            }
            return 0;
        }

        return Duration.between(previousPointAt, currentPointAt).getSeconds();
    }

    //외출 확정됐을 때 외출 candidate 시간 누적
    public long calculateConfirmedOutingDurationSeconds(HomeZoneStatus transitionedZoneStatus,
        Instant transitionTime, Instant currentPointAt) {
        if (transitionedZoneStatus != HomeZoneStatus.OUT_HOME) {
            return 0;
        }

        return Duration.between(transitionTime, currentPointAt).getSeconds();
    }
}
