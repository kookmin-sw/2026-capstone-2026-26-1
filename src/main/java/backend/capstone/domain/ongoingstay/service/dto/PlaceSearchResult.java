package backend.capstone.domain.ongoingstay.service.dto;

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
