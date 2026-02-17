package backend.capstone.domain.gpspoint.repository;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.dto.GpsPointRecordedAtRange;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
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
}
