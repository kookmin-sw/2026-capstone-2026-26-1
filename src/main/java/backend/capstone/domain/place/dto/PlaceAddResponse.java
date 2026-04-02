package backend.capstone.domain.place.dto;

import backend.capstone.domain.place.entity.PlaceSource;
import lombok.Builder;

@Builder
public record PlaceAddResponse(
    Long placeId,
    String placeName,
    PlaceSource type,
    String roadAddress,
    double latitude,
    double longitude,
    int orderIndex
) {

}
