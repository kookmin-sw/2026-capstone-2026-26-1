package backend.capstone.domain.place.service.dto;

import lombok.Builder;

@Builder
public record PlaceSearchResult(
    String name,
    String roadAddress,
    String jibunAddress,
    double latitude,
    double longitude
) {

}
