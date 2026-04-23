package backend.capstone.domain.place.mapper;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.ongoingstay.service.dto.PlaceSearchResult;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceItem;
import backend.capstone.domain.place.dto.PlaceListResponse;
import backend.capstone.domain.kakaoplace.dto.PlaceSearchResponse;
import backend.capstone.domain.kakaoplace.dto.PlaceSearchResponse.PlaceSearchItem;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import backend.capstone.domain.place.entity.Place;
import backend.capstone.domain.place.entity.PlaceSource;
import backend.capstone.domain.kakaoplace.service.dto.KakaoLocalSearchResult;
import java.util.List;
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
            .type(place.getSource())
            .roadAddress(place.getRoadAddress())
            .latitude(place.getLatitude())
            .longitude(place.getLongitude())
            .orderIndex(place.getOrderIndex())
            .build();
    }

    public static PlaceItem toPlaceItem(Place place) {
        return PlaceItem.builder()
            .placeId(place.getId())
            .placeName(place.getName())
            .type(place.getSource())
            .roadAddress(place.getRoadAddress())
            .latitude(place.getLatitude())
            .longitude(place.getLongitude())
            .orderIndex(place.getOrderIndex())
            .build();
    }

    public static PlaceListResponse toPlaceListResponse(List<Place> places) {
        List<PlaceItem> items = places.stream()
            .map(PlaceMapper::toPlaceItem)
            .toList();

        return PlaceListResponse.builder()
            .placeCount(items.size())
            .places(items)
            .build();
    }

    public static PlaceUpdateResponse toPlaceUpdateResponse(Place place) {
        return PlaceUpdateResponse.builder()
            .roadAddress(place.getRoadAddress())
            .placeName(place.getName())
            .type(place.getSource())
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
