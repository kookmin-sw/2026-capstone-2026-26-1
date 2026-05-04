package backend.capstone.domain.gpspoint.dto;

import java.time.Instant;

public record GpsPointRecordedAtRange(
    Instant startTime,
    Instant endTime
) {

}
