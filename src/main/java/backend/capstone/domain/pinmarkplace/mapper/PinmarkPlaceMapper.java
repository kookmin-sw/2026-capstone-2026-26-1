package backend.capstone.domain.pinmarkplace.mapper;

import backend.capstone.domain.pinmarkplace.dto.PinmarkPlaceCreateRequest;
import backend.capstone.domain.pinmarkplace.dto.PinmarkPlaceCreateResponse;
import backend.capstone.domain.pinmarkplace.entity.PinmarkPlace;
import backend.capstone.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PinmarkPlaceMapper {

    public static PinmarkPlace toEntity(User user, PinmarkPlaceCreateRequest request) {
        return PinmarkPlace.builder()
            .user(user)
            .type(request.type())
            .name(request.placeName())
            .roadAddress(request.roadAddress())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .build();
    }

    public static PinmarkPlaceCreateResponse toCreateResponse(PinmarkPlace pinmarkPlace) {
        return PinmarkPlaceCreateResponse.builder()
            .pinmarkPlaceId(pinmarkPlace.getId())
            .type(pinmarkPlace.getType())
            .placeName(pinmarkPlace.getName())
            .roadAddress(pinmarkPlace.getRoadAddress())
            .latitude(pinmarkPlace.getLatitude())
            .longitude(pinmarkPlace.getLongitude())
            .build();
    }
}
