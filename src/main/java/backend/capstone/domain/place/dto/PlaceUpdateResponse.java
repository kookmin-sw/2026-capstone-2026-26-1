package backend.capstone.domain.place.dto;

import lombok.Builder;

@Builder
public record PlaceUpdateResponse(
    String roadAddress,
    String placeName
) {

}
