package backend.capstone.domain.ongoingstay.entity;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class OngoingStay extends BaseTimeEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "stay_cluster_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_route_id")
    private DayRoute dayRoute;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_point_id")
    private GpsPoint startPoint;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_point_id")
    private GpsPoint endPoint;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private double centerLatitude;

    private double centerLongitude;

    @Enumerated(EnumType.STRING)
    private OngoingStayStatus status;

    private int pointCount;

    public static OngoingStay start(DayRoute dayRoute, GpsPoint point) {
        OngoingStay stay = new OngoingStay();
        stay.dayRoute = dayRoute;
        stay.centerLatitude = point.getLatitude();
        stay.centerLongitude = point.getLongitude();
        stay.startPoint = point;
        stay.endPoint = point;
        stay.startTime = point.getRecordedAt();
        stay.endTime = point.getRecordedAt();
        stay.pointCount = 1;
        stay.status = OngoingStayStatus.IN_PROGRESS;
        return stay;
    }

    public void addPoint(GpsPoint point) {
        this.centerLatitude =
            ((this.centerLatitude * this.pointCount) + point.getLatitude())
                / (this.pointCount + 1);

        this.centerLongitude =
            ((this.centerLongitude * this.pointCount) + point.getLongitude())
                / (this.pointCount + 1);

        this.pointCount++;
        this.endPoint = point;
        this.endTime = point.getRecordedAt();
    }

    public long getDurationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }
}

