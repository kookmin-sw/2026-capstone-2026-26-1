package backend.capstone.domain.gpspoint.entity;

import backend.capstone.domain.dayroute.entity.DayRoute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "gps_point",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_day_route_recorded_at", columnNames = {"day_route_id",
            "recorded_at"})
    }
)
public class GpsPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gps_point_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "day_route_id")
    private DayRoute dayRoute;

//    private String deviceId;

    private double latitude;

    private double longitude;

    private LocalDateTime recordedAt;
}
