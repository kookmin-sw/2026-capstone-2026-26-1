package backend.capstone.domain.place.mapper;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.ongoingstay.service.dto.PlaceSearchResult;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import backend.capstone.domain.place.entity.Place;
import backend.capstone.domain.place.entity.PlaceSource;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PlaceMapper {

    public static Place toEntityByManual(DayRoute dayRoute, PlaceAddRequest request,
        int orderIndex) {
        return Place.builder()
            .dayRoute(dayRoute)
            .roadAddress(request.roadAddress())
            .name(request.placeName())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .orderIndex(orderIndex)
            .source(PlaceSource.MANUAL)
            .build();
    }

    public static PlaceAddResponse toPlaceAddResponse(Place place) {
        return PlaceAddResponse.builder()
            .placeId(place.getId())
            .placeName(place.getName())
            .roadAddress(place.getRoadAddress())
            .latitude(place.getLatitude())
            .longitude(place.getLongitude())
            .orderIndex(place.getOrderIndex())
            .build();
    }

    public static PlaceUpdateResponse toPlaceUpdateResponse(Place place) {
        return PlaceUpdateResponse.builder()
            .roadAddress(place.getRoadAddress())
            .placeName(place.getName())
            .latitude(place.getLatitude())
            .longitude(place.getLongitude())
            .build();
    }

    public static Place toEntityByAuto(DayRoute dayRoute, PlaceSearchResult searchResult,
        int orderIndex) {
        return Place.builder()
            .dayRoute(dayRoute)
            .name(firstNonBlank(searchResult.name(), searchResult.roadAddress(),
                searchResult.jibunAddress()))
            .roadAddress(firstNonBlank(searchResult.roadAddress(), searchResult.jibunAddress()))
            .latitude(searchResult.latitude())
            .longitude(searchResult.longitude())
            .orderIndex(orderIndex)
            .source(PlaceSource.AUTO)
            .build();
    }

    public static Place toUnknownAuto(DayRoute dayRoute, double stayLatitude,
        double stayLongitude, int orderIndex) {
        return Place.builder()
            .dayRoute(dayRoute)
            .name("알 수 없음")
            .roadAddress(null)
            .latitude(stayLatitude)
            .longitude(stayLongitude)
            .orderIndex(orderIndex)
            .source(PlaceSource.AUTO)
            .build();
    }

    public static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
