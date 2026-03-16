package backend.capstone.domain.place.dto;

import lombok.Builder;

@Builder
public record PlaceAddResponse(
    Long placeId,
    String placeName,
    String roadAddress,
    int orderIndex
) {

}
