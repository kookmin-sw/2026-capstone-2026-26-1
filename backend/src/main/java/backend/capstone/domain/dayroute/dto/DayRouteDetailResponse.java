package backend.capstone.domain.dayroute.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
        @Schema(example = "2026-04-29T13:32:43.059+09:00")
        OffsetDateTime recordedAt,
        double latitude,
        double longitude
    ) {

    }
}
