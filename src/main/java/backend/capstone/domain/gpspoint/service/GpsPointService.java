package backend.capstone.domain.gpspoint.service;

import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest.GpsPointRequest;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.dto.GpsPointRecordedAtRange;
import backend.capstone.domain.gpspoint.repository.GpsPointRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GpsPointService {

    private final JdbcTemplate jdbcTemplate;
    private final GpsPointRepository gpsPointRepository;

    @Transactional
    public void batchInsert(Long dayRouteId, GpsPointBatchUploadRequest request) {

        //batch insert
        String sql = """
                INSERT IGNORE INTO gps_point(day_route_id, latitude, longitude, recorded_at)
                VALUES (?, ?, ?, ?)
            """;

        List<GpsPointRequest> points = request.gpsPoints();

        jdbcTemplate.batchUpdate(sql, points, points.size(), (ps, gpsPoint) -> {
            ps.setLong(1, dayRouteId);
            ps.setDouble(2, gpsPoint.latitude());
            ps.setDouble(3, gpsPoint.longitude());
            ps.setObject(4, gpsPoint.recordedAt());
        });

    }

    @Transactional(readOnly = true)
    public GpsPointRecordedAtRange getGpsPointRange(DayRoute dayRoute) {
        return gpsPointRepository.findRecordedAtRange(dayRoute);
    }

}
