package backend.capstone.domain.gpspoint.dto;

import java.time.LocalDateTime;

public record GpsPointRecordedAtRange(
    LocalDateTime startTime,
    LocalDateTime endTime
) {

}
