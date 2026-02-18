package backend.capstone.domain.dayroute.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//TODO: 장소 목록, km, 즐찾여부 추가
public record DayRouteDetailResponse(
    LocalDate date,
    List<GpsPointListResponse> gpsPoints
) {

    public record GpsPointListResponse(
        LocalDateTime recordedAt,
        double latitude,
        double longitude
    ) {

    }

}
