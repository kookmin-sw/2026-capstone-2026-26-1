package backend.capstone.domain.kakaoplace.dto;

import lombok.Builder;

@Builder
public record SearchResultByCategoryAndCoord(
    String name,
    String roadAddress,
    String jibunAddress,
    double latitude,
    double longitude
) {

}
