package backend.capstone.domain.kakaoplace.dto;

import java.util.List;

public record PlaceSearchResponse(
    int placeCount,
    List<PlaceSearchItem> places
) {
    public record PlaceSearchItem(
        String placeName,
        String category,
        String roadAddress,
        Double longitude,
        Double latitude
    ) {
    }
}
