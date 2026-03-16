package backend.capstone.domain.dayroute.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record GpsPointsResponse(
    List<GpsPointItem> gpsPoints
) {

    public record GpsPointItem(
        LocalDateTime recordedAt,
        double latitude,
        double longitude
    ) {

    }

}
