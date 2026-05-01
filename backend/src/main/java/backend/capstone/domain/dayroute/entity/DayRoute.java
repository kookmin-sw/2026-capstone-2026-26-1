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

    private Instant startTime;

    private Instant endTime;

    private double totalDistance;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String memo;

    private boolean deleted;

    private boolean isBookmarked;

    @OneToMany(mappedBy = "dayRoute")
    private List<GpsPoint> gpsPoints;

    @Column(columnDefinition = "LONGTEXT")
    private String encodedPath;

    private Integer pathPointCount;

    private boolean hasPolyline;

    private boolean hasDetails;

    // stay 분석 flag
    private boolean analysisNeeded;

    private Instant lastAnalyzedAt;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus;

    @Enumerated(EnumType.STRING)
    private DayRouteHomeStatus dayRouteHomeStatus;

    //외출시간
    private Instant exitHomeTime;

    //귀가시간
    private Instant enterHomeTime;

    private Instant homeAnalysisLastPointAt;

    @Builder
    public DayRoute(User user, LocalDate date) {
        this.user = user;
        this.date = date;
        gpsPoints = new ArrayList<>();
        analysisStatus = AnalysisStatus.IDLE;
        dayRouteHomeStatus = DayRouteHomeStatus.UNKNOWN;
    }

    public void updateTime(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
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

    // 분석용 업데이트
    public void markAnalysisNeeded() {
        this.analysisNeeded = true;
    }

    public void markInProgressAnalysis() {
        this.analysisStatus = AnalysisStatus.IN_PROGRESS;
    }

    public void markIdleAnalysis() {
        this.analysisNeeded = false;
        this.analysisStatus = AnalysisStatus.IDLE;
    }

    public void completeAnalysis(Instant recordedAt) {
        lastAnalyzedAt = recordedAt;
        markIdleAnalysis();
    }

    public void markAtHome() {
        this.dayRouteHomeStatus = DayRouteHomeStatus.AT_HOME;
    }

    public void markOuting(Instant outingTime) {
        this.dayRouteHomeStatus = DayRouteHomeStatus.OUTING;
        if (this.exitHomeTime == null) {
            this.exitHomeTime = outingTime;
        }
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

    public void updateHomeAnalysisLastPointAt(Instant homeAnalysisLastPointAt) {
        this.homeAnalysisLastPointAt = homeAnalysisLastPointAt;
    }
}
