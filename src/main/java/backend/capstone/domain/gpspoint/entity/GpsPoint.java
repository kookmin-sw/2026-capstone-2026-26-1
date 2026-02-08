package backend.capstone.domain.gpspoint.entity;

import backend.capstone.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GpsPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gps_point_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String deviceId;

    private double latitude;

    private double longitude;

    private LocalDateTime recordedAt;

    @Builder
    public GpsPoint(User user, String deviceId, double latitude, double longitude,
        LocalDateTime recordedAt) {
        this.user = user;
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = recordedAt;
    }
}
