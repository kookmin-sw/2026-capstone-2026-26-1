package backend.capstone.domain.dayroute.mapper;

import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMonthlyResponse;
import backend.capstone.domain.dayroute.dto.GpsPointsResponse;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.place.dto.PlaceItem;
import backend.capstone.domain.place.entity.Place;
import backend.capstone.domain.user.entity.User;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
                .map(p -> PlaceItem.builder()
                    .placeId(p.getId())
                    .placeName(p.getName())
                    .type(p.getSource())
                    .roadAddress(p.getRoadAddress())
                    .latitude(p.getLatitude())
                    .longitude(p.getLongitude())
                    .orderIndex(p.getOrderIndex())
                    .build())
                .toList())
            .build();
    }

    public static DayRouteMonthlyResponse toDayRouteMonthlyResponse(int year, int month,
        List<DayRoute> dayRoutes) {
        YearMonth yearMonth = YearMonth.of(year, month);
        // DayRoute 목록을 날짜 기준 map으로 변경
        Map<LocalDate, DayRoute> dayRouteMap = dayRoutes.stream()
            .collect(Collectors.toMap(DayRoute::getDate, Function.identity()));

        return DayRouteMonthlyResponse.builder()
            .year(year)
            .month(month)
            .days(yearMonth.atDay(1).datesUntil(yearMonth.atEndOfMonth().plusDays(1))
                .map(date -> {
                    DayRoute dayRoute = dayRouteMap.get(date);
                    return DayRouteMonthlyResponse.DayItem.builder()
                        .date(date)
                        .dayRouteExists(dayRoute != null)
                        .dayRoute(dayRoute == null ? null
                            : DayRouteMonthlyResponse.DayRouteItem.builder()
                                .hasLocationData(dayRoute.isHasGpsPoints())
                                .hasManualData(dayRoute.isHasManualData())
                                .isBookmarked(dayRoute.isBookmarked())
                                .build())
                        .build();
                })
                .toList())
            .build();
    }
}
