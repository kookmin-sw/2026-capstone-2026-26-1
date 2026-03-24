package backend.capstone.domain.pinmarkplace.dto;

import backend.capstone.domain.place.entity.PinmarkPlaceType;

public record PinmarkPlaceCreateRequest(
    PinmarkPlaceType type,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude
) {

}
