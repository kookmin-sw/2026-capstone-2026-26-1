package backend.capstone.domain.dayroute.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record DayRouteDetailResponse(
    LocalDate date,
    double totalDistance,
    String title,
    String memo,
    boolean isBookmarked,
//    String encodedPath,
//    Integer pathPointCount,
    List<GpsPointItem> gpsPoints
) {

    public record GpsPointItem(
        LocalDateTime recordedAt,
        double latitude,
        double longitude
    ) {

    }
}
