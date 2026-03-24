package backend.capstone.domain.pinmarkplace.dto;

import backend.capstone.domain.place.entity.PinmarkPlaceType;
import lombok.Builder;

@Builder
public record PinmarkPlaceCreateResponse(
    Long pinmarkPlaceId,
    PinmarkPlaceType type,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude
) {

}
