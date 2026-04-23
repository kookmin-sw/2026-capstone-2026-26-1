package backend.capstone.domain.place.dto;

import java.util.List;

public record PlaceSearchResponse(
    int placeCount,
    List<PlaceSearchItem> items
) {
    public record PlaceSearchItem(
        String placeName,
        String category,
        String address,
        String roadAddress,
        Integer longitude,
        Integer latitude
    ) {
    }
}
