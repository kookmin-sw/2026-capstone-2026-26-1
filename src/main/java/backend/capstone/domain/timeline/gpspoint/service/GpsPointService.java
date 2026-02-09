package backend.capstone.domain.timeline.gpspoint.service;

import backend.capstone.domain.timeline.gpspoint.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.timeline.gpspoint.dto.GpsPointBatchUploadRequest.GpsPointRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GpsPointService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchInsert(Long userId, GpsPointBatchUploadRequest request) {

        //batch insert
        String sql = """
                INSERT IGNORE INTO gps_point(user_id, latitude, longitude, recorded_at)
                VALUES (?, ?, ?, ?)
            """;

        List<GpsPointRequest> points = request.gpsPoints();

        jdbcTemplate.batchUpdate(sql, points, points.size(), (ps, gpsPoint) -> {
            ps.setLong(1, userId);
            ps.setDouble(2, gpsPoint.latitude());
            ps.setDouble(3, gpsPoint.longitude());
            ps.setObject(4, gpsPoint.recordedAt());
        });

    }

}
