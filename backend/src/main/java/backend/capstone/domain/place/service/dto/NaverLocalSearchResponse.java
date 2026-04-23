package backend.capstone.domain.place.service.dto;

import java.util.List;

public record NaverLocalSearchResponse(
    List<NaverLocalSearchItem> items
) {

    public record NaverLocalSearchItem(
        String title,
        String category,
        String address,
        String roadAddress,
        Integer mapx,
        Integer mapy
    ) {
    }

}
