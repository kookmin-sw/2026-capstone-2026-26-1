package backend.capstone.domain.ongoingstay.entity;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @Column(name = "ongoing_stay_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_route_id")
    private DayRoute dayRoute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_point_id")
    private GpsPoint startPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_point_id")
    private GpsPoint lastPoint;

    private LocalDateTime startTime;

    private LocalDateTime lastTime;

    private double centerLatitude;

    private double centerLongitude;

    private int pointCount;

    public static OngoingStay start(DayRoute dayRoute, GpsPoint point) {
        OngoingStay stay = new OngoingStay();
        stay.dayRoute = dayRoute;
        stay.centerLatitude = point.getLatitude();
        stay.centerLongitude = point.getLongitude();
        stay.startPoint = point;
        stay.lastPoint = point;
        stay.startTime = point.getRecordedAt();
        stay.lastTime = point.getRecordedAt();
        stay.pointCount = 1;
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
        this.lastPoint = point;
        this.lastTime = point.getRecordedAt();
    }

    public long getDurationMinutes() {
        return Duration.between(startTime, lastTime).toMinutes();
    }

    public void updateLastTime(LocalDateTime lastTime) {
        this.lastTime = lastTime;
    }
}
