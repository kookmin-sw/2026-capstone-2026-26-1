package backend.capstone.domain.dayroute.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record DayRouteDetailResponse(
    LocalDate date,
    double totalDistance,
    String title,
    String memo,
    boolean isBookmarked,
    String encodedPath,
    Integer pathPointCount,
    List<PlaceItem> places
) {

    @Builder
    public record PlaceItem(
        Long placeId,
        String placeName,
        String roadAddress,
        double latitude,
        double longitude,
        int orderIndex
    ) {

    }

}
