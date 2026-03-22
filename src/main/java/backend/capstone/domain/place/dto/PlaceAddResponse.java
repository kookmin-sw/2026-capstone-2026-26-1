package backend.capstone.domain.place.dto;

import lombok.Builder;

@Builder
public record PlaceAddResponse(
    Long placeId,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude,
    int orderIndex
) {

}
