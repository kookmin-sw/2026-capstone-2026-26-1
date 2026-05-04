package backend.capstone.domain.ongoinghomestatus.service;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.ongoinghomestatus.entity.HomeZoneStatus;
import backend.capstone.domain.ongoinghomestatus.entity.OngoingHomeStatus;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HomeStatusTransitionHandler {

    private static final int TRANSITION_MINUTES = 3;

    private final OutingDurationAccumulator outingDurationAccumulator;

    public void applyInitialDayRouteStatus(DayRoute dayRoute, HomeZoneStatus zoneStatus) {
        if (zoneStatus == HomeZoneStatus.IN_HOME) {
            dayRoute.markAtHome();
            return;
        }

        dayRoute.markOutingWithoutTime();
    }

    public long handleTransition(DayRoute dayRoute, OngoingHomeStatus ongoingHomeStatus,
        HomeZoneStatus observedZoneStatus, Instant observedAt) {
        if (observedZoneStatus == ongoingHomeStatus.getCurrentZoneStatus()) {
            ongoingHomeStatus.clearCandidate();
            return 0;
        }

        if ((ongoingHomeStatus.getCandidateZoneStatus() == null)
            || (ongoingHomeStatus.getCandidateZoneStatus() != observedZoneStatus)) {
            ongoingHomeStatus.startCandidate(observedZoneStatus, observedAt);
            return 0;
        }

        long candidateDurationMinutes = Duration.between(ongoingHomeStatus.getCandidateStartedAt(),
            observedAt).toMinutes();

        if (candidateDurationMinutes < TRANSITION_MINUTES) {
            return 0;
        }

        Instant transitionTime = ongoingHomeStatus.getCandidateStartedAt();
        ongoingHomeStatus.changeCurrentZoneStatus(observedZoneStatus, transitionTime);
        applyTransitionedDayRouteStatus(dayRoute, observedZoneStatus, transitionTime);

        return outingDurationAccumulator.calculateConfirmedOutingDurationSeconds(observedZoneStatus,
            transitionTime, observedAt);
    }

    private void applyTransitionedDayRouteStatus(DayRoute dayRoute, HomeZoneStatus zoneStatus,
        Instant transitionTime) {
        if (zoneStatus == HomeZoneStatus.IN_HOME) {
            dayRoute.markReturnedHome(transitionTime);
            return;
        }

        dayRoute.markOuting(transitionTime);
    }
}
