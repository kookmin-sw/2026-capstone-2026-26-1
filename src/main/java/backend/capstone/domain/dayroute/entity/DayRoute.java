package backend.capstone.domain.dayroute.entity;

import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "day_route",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_date", columnNames = {"user_id", "date"})
    }
)
public class DayRoute {

    @Id
    @Column(name = "day_route_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate date;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private double totalDistance;

    @Column(columnDefinition = "TEXT")
    private String memo;

    private boolean deleted;

    private boolean bookmarked;

    @OneToMany(mappedBy = "dayRoute")
    @OrderBy("recordedAt ASC")
    private List<GpsPoint> gpsPoints;

    @Builder
    public DayRoute(User user, LocalDate date) {
        this.user = user;
        this.date = date;
        gpsPoints = new ArrayList<>();
    }

    public void updateTime(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
