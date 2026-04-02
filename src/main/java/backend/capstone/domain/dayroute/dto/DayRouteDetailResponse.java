package backend.capstone.domain.dayroute.dto;

import backend.capstone.domain.place.dto.PlaceItem;
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
    int placeCount,
    List<PlaceItem> places
) {
}
