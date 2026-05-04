package backend.capstone.domain.gpspoint.repository;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.dto.GpsPointRecordedAtRange;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GpsPointRepository extends
    JpaRepository<GpsPoint, Long> {

    @Query("""
            select new backend.capstone.domain.gpspoint.dto.GpsPointRecordedAtRange(
                min(g.recordedAt),
                max(g.recordedAt)
            )
            from GpsPoint g
            where g.dayRoute = :dayRoute
        """)
    GpsPointRecordedAtRange findRecordedAtRange(@Param("dayRoute") DayRoute dayRoute);

    @Query("""
            select gp
            from GpsPoint gp
            where gp.dayRoute = :dayRoute
            order by gp.recordedAt asc
        """)
    List<GpsPoint> findByDayRouteIdOrderByRecordedAt(@Param("dayRoute") DayRoute dayRoute);

    @Query("""
            select gp
            from GpsPoint gp
            where gp.dayRoute = :dayRoute
              and gp.recordedAt > :lastRecordedAt
            order by gp.recordedAt asc
        """)
    List<GpsPoint> findNewPointsAfterCursor(
        @Param("dayRoute") DayRoute dayRoute,
        @Param("lastRecordedAt") Instant lastRecordedAt
    );

    List<GpsPoint> findByDayRouteOrderByRecordedAtAsc(@Param("dayRoute") DayRoute dayRoute);
}
