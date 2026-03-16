package backend.capstone.domain.gpspoint.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GpsPointRecordedAtRange {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
