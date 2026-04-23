package backend.capstone.domain.place.dto;

import java.util.List;

public record PlaceSearchResponse(
    int placeCount,
    List<PlaceSearchItem> places
) {
    public record PlaceSearchItem(
        String placeName,
        String category,
        String roadAddress,
        Integer longitude,
        Integer latitude
    ) {
    }
}
