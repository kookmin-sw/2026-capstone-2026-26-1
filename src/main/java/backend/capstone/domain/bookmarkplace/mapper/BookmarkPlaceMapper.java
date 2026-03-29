package backend.capstone.domain.bookmarkplace.mapper;

import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookmarkPlaceMapper {

    public static BookmarkPlace toEntity(User user, BookmarkPlaceCreateRequest request) {
        return BookmarkPlace.builder()
            .user(user)
            .type(request.type())
            .name(request.placeName())
            .roadAddress(request.roadAddress())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .build();
    }

    public static BookmarkPlaceCreateResponse toCreateResponse(BookmarkPlace bookmarkPlace) {
        return BookmarkPlaceCreateResponse.builder()
            .bookmarkPlaceId(bookmarkPlace.getId())
            .type(bookmarkPlace.getType())
            .placeName(bookmarkPlace.getName())
            .roadAddress(bookmarkPlace.getRoadAddress())
            .latitude(bookmarkPlace.getLatitude())
            .longitude(bookmarkPlace.getLongitude())
            .build();
    }
}
