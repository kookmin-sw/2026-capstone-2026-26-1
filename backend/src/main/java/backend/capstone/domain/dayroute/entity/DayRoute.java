package backend.capstone.domain.dayroute.entity;

import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.user.entity.User;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Id
    @Column(name = "day_route_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate date;

    private Instant startTime; //route 시작 시간

    private Instant endTime; //route 종료 시간

    private double totalDistance;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String memo;

    private boolean deleted;

    private boolean isBookmarked;

    @OneToMany(mappedBy = "dayRoute")
    private List<GpsPoint> gpsPoints;

//    @Column(columnDefinition = "LONGTEXT")
//    private String encodedPath;
//
//    private Integer pathPointCount;

    private boolean hasPolyline;

    private boolean hasDetails;

    private Instant lastAnalyzedAt;

    @Enumerated(EnumType.STRING)
    private DayRouteHomeStatus dayRouteHomeStatus;

    //첫 외출 시간
    private Instant outingTime;

    //마지막 귀가 시간
    private Instant enterHomeTime;

    //총 외출 횟수
    private int totalOutingCount;

    //외출 시간
    private long totalOutingSeconds;

    private Instant homeAnalysisLastPointAt;

    @Builder
    public DayRoute(User user, LocalDate date) {
        this.user = user;
        this.date = date;
        initializeRouteWindow();
        gpsPoints = new ArrayList<>();
        dayRouteHomeStatus = DayRouteHomeStatus.UNKNOWN;
    }

//    public void updateEncodedPath(String encodedPath, int pathPointCount) {
//        this.encodedPath = encodedPath;
//        this.pathPointCount = pathPointCount;
//    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void markHasGpsPoints() {
        this.hasPolyline = true;
    }

    public void updateHasManualData(boolean hasManualData) {
        this.hasDetails = hasManualData;
    }

    public boolean toggleBookmarked() {
        isBookmarked = !isBookmarked;
        return isBookmarked;
    }

    public void updateDistance(double distance) {
        this.totalDistance = distance;
    }

    public void completeAnalysis(Instant recordedAt) {
        this.lastAnalyzedAt = recordedAt;
    }

    public void markAtHome() {
        this.dayRouteHomeStatus = DayRouteHomeStatus.AT_HOME;
    }

    public void markOuting(Instant outingTime) {
        this.dayRouteHomeStatus = DayRouteHomeStatus.OUTING;
        if (this.outingTime == null) {
            this.outingTime = outingTime;
        }
        this.totalOutingCount++;
    }

    public void markOutingWithoutTime() {
        this.dayRouteHomeStatus = DayRouteHomeStatus.OUTING;
    }

    public void markReturnedHome(Instant homeComingTime) {
        this.dayRouteHomeStatus = DayRouteHomeStatus.RETURNED_HOME;
        this.enterHomeTime = homeComingTime;
    }

    public void markNoHomeBookmark() {
        this.dayRouteHomeStatus = DayRouteHomeStatus.NO_HOME_BOOKMARK;
    }

    public void addOutingDurationSeconds(long outingSeconds) {
        if (outingSeconds <= 0) {
            return;
        }

        this.totalOutingSeconds += outingSeconds;
    }

    public void updateHomeAnalysisLastPointAt(Instant homeAnalysisLastPointAt) {
        this.homeAnalysisLastPointAt = homeAnalysisLastPointAt;
    }

    private void initializeRouteWindow() {
        //TODO: @notnull 제약조건으로 제거
        if (user == null || date == null) {
            return;
        }

        LocalTime routeStartTime = user.getDayStartTime();
        LocalTime routeEndTime = user.getDayEndTime();
        LocalDateTime routeStartDateTime = LocalDateTime.of(date, routeStartTime);

        LocalDate endDate = date;
        if (routeEndTime.isBefore(routeStartTime) || routeEndTime.equals(routeStartTime)) {
            endDate = date.plusDays(1);
        }

        LocalDateTime routeEndDateTime = LocalDateTime.of(endDate, routeEndTime);
        this.startTime = routeStartDateTime.atZone(KST_ZONE_ID).toInstant();
        this.endTime = routeEndDateTime.atZone(KST_ZONE_ID).toInstant();
    }
}
