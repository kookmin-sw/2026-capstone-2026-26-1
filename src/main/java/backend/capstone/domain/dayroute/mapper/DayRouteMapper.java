package backend.capstone.domain.dayroute.mapper;

import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.GpsPointsResponse;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.place.entity.Place;
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

    public static GpsPointsResponse toGpsPointsResponse(List<GpsPoint> gpsPoints) {
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

    public static DayRouteDetailResponse toDayRouteDetailResponse(DayRoute dayRoute,
        List<Place> places) {
        return DayRouteDetailResponse.builder()
            .date(dayRoute.getDate())
            .totalDistance(dayRoute.getTotalDistance())
            .title(dayRoute.getTitle())
            .memo(dayRoute.getMemo())
            .isBookmarked(dayRoute.isBookmarked())
            .encodedPath(dayRoute.getEncodedPath())
            .pathPointCount(dayRoute.getPathPointCount())
            .places(places.stream()
                .map(p -> DayRouteDetailResponse.PlaceItem.builder()
                    .placeId(p.getId())
                    .placeName(p.getName())
                    .roadAddress(p.getRoadAddress())
                    .orderIndex(p.getOrderIndex())
                    .build())
                .toList())
            .build();
    }
}
