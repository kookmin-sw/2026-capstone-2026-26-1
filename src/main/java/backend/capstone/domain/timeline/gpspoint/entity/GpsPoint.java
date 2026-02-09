package backend.capstone.domain.timeline.gpspoint.entity;

import backend.capstone.domain.user.entity.User;
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
        @UniqueConstraint(name = "uk_user_recorded_at", columnNames = {"user_id", "recorded_at"})
    } //TODO: user_id대신 device_id 넣기
)
public class GpsPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gps_point_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

//    private String deviceId;

    private double latitude;

    private double longitude;

    private LocalDateTime recordedAt;
}
