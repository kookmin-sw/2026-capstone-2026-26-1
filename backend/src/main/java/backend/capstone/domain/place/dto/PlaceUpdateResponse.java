package backend.capstone.domain.place.dto;

import backend.capstone.domain.place.entity.PlaceSource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record PlaceUpdateResponse(
    String roadAddress,
    String placeName,
    PlaceSource source,
    double latitude,
    double longitude,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
