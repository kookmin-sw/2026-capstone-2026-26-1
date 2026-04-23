package backend.capstone.domain.place.dto;

import backend.capstone.domain.place.entity.PlaceSource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record PlaceAddResponse(
    Long placeId,
    String placeName,
    PlaceSource source,
    String roadAddress,
    double latitude,
    double longitude,
    int orderIndex,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
