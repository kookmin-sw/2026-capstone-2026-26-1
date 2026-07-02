package backend.capstone.domain.mobility.analysis.ongoinghomestatus.service;

import backend.capstone.domain.mobility.analysis.ongoinghomestatus.entity.HomeZoneStatus;
import backend.capstone.domain.mobility.analysis.ongoinghomestatus.entity.OngoingHomeStatus;
import backend.capstone.domain.mobility.dayroute.entity.DayRoute;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class HomeStatusTransitionHandler {

    public void applyInitialDayRouteStatus(DayRoute dayRoute, HomeZoneStatus zoneStatus) {
        if (zoneStatus == HomeZoneStatus.IN_HOME) {
            dayRoute.markAtHome();
            return;
        }

        dayRoute.markOutingWithoutTime();
    }

    public void handleTransition(DayRoute dayRoute, OngoingHomeStatus ongoingHomeStatus,
        HomeZoneStatus observedZoneStatus, Instant observedAt) {
        if (observedZoneStatus == ongoingHomeStatus.getCurrentZoneStatus()) {
            return;
        }

        ongoingHomeStatus.changeCurrentZoneStatus(observedZoneStatus, observedAt);
        applyTransitionedDayRouteStatus(dayRoute, observedZoneStatus, observedAt);
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
