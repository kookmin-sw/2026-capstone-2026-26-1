package backend.capstone.domain.dayroute.mapper;

import backend.capstone.domain.dayroute.dto.GpsPointsResponse;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DayRouteMapper {

    public static DayRoute toEntity(User user, LocalDate date) {
        return DayRoute.builder()
            .user(user)
            .date(date)
            .build();
    }

    public static GpsPointsResponse toGpsPointListResponse(List<GpsPoint> gpsPoints) {
        return GpsPointsResponse.builder()
            .gpsPoints(gpsPoints.stream()
                .map(gp -> new GpsPointsResponse.GpsPointItem(
                    gp.getRecordedAt(),
                    gp.getLatitude(),
                    gp.getLongitude()
                ))
                .toList())
            .build();
    }
}
