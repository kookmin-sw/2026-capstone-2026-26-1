package backend.capstone.domain.place.mapper;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import backend.capstone.domain.place.entity.Place;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PlaceMapper {

    public static Place toEntity(DayRoute dayRoute, PlaceAddRequest request, int orderIndex) {
        return Place.builder()
            .dayRoute(dayRoute)
            .roadAddress(request.roadAddress())
            .name(request.placeName())
            .orderIndex(orderIndex)
            .build();
    }

    public static PlaceAddResponse toPlaceAddResponse(Place place) {
        return PlaceAddResponse.builder()
            .placeId(place.getId())
            .placeName(place.getName())
            .roadAddress(place.getRoadAddress())
            .orderIndex(place.getOrderIndex())
            .build();
    }

    public static PlaceUpdateResponse toPlaceUpdateResponse(Place place) {
        return PlaceUpdateResponse.builder()
            .roadAddress(place.getRoadAddress())
            .placeName(place.getName())
            .build();
    }

}
