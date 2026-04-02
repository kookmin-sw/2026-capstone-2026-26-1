package backend.capstone.domain.place.dto;

import backend.capstone.domain.place.entity.PlaceSource;
import lombok.Builder;

@Builder
public record PlaceUpdateResponse(
    String roadAddress,
    String placeName,
    PlaceSource type,
    double latitude,
    double longitude
) {

}
