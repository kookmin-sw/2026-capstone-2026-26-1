package backend.capstone.domain.ongoinghomestatus.entity;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "ongoing_home_status",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ongoing_home_status_day_route", columnNames = "day_route_id")
    }
)
public class OngoingHomeStatus extends BaseTimeEntity {

    @Id
    @Column(name = "ongoing_home_status_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_route_id", nullable = false)
    private DayRoute dayRoute;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HomeZoneStatus currentZoneStatus;

    @Enumerated(EnumType.STRING)
    private HomeZoneStatus candidateZoneStatus;

    private Instant candidateStartedAt;

    private Instant lastTransitionAt;

    public static OngoingHomeStatus initialize(DayRoute dayRoute, GpsPoint point,
        HomeZoneStatus currentZoneStatus
    ) {
        OngoingHomeStatus status = new OngoingHomeStatus();
        status.dayRoute = dayRoute;
        status.currentZoneStatus = currentZoneStatus;
        status.lastTransitionAt = point.getRecordedAt();
        return status;
    }

    public void startCandidate(HomeZoneStatus candidateZoneStatus, Instant candidateStartedAt) {
        this.candidateZoneStatus = candidateZoneStatus;
        this.candidateStartedAt = candidateStartedAt;
    }

    public void clearCandidate() {
        this.candidateZoneStatus = null;
        this.candidateStartedAt = null;
    }

    public void changeCurrentZoneStatus(HomeZoneStatus currentZoneStatus, Instant transitionedAt) {
        this.currentZoneStatus = currentZoneStatus;
        this.lastTransitionAt = transitionedAt;
        clearCandidate();
    }
}
