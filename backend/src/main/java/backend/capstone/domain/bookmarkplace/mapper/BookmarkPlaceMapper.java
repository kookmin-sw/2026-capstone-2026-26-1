package backend.capstone.domain.bookmarkplace.mapper;

import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceListResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceSummaryResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceUpdateResponse;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.place.service.dto.Wgs84Coordinate;
import backend.capstone.domain.user.entity.User;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookmarkPlaceMapper {

    public static BookmarkPlace toEntity(
        User user,
        BookmarkPlaceCreateRequest request,
        Wgs84Coordinate coordinate
    ) {
        return BookmarkPlace.builder()
            .user(user)
            .type(request.type())
            .name(request.placeName())
            .roadAddress(request.roadAddress())
            .latitude(coordinate.latitude())
            .longitude(coordinate.longitude())
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

    public static BookmarkPlaceUpdateResponse toUpdateResponse(BookmarkPlace bookmarkPlace) {
        return BookmarkPlaceUpdateResponse.builder()
            .type(bookmarkPlace.getType())
            .placeName(bookmarkPlace.getName())
            .roadAddress(bookmarkPlace.getRoadAddress())
            .latitude(bookmarkPlace.getLatitude())
            .longitude(bookmarkPlace.getLongitude())
            .build();
    }

    public static BookmarkPlaceListResponse toListResponse(List<BookmarkPlace> bookmarkPlaces) {
        return BookmarkPlaceListResponse.builder()
            .placeCount(bookmarkPlaces.size())
            .bookmarkPlaces(bookmarkPlaces.stream()
                .map(BookmarkPlaceMapper::toSummaryResponse)
                .toList())
            .build();
    }

    public static BookmarkPlaceSummaryResponse toSummaryResponse(BookmarkPlace bookmarkPlace) {
        return BookmarkPlaceSummaryResponse.builder()
            .placeId(bookmarkPlace.getId())
            .placeName(bookmarkPlace.getName())
            .type(bookmarkPlace.getType())
            .roadAddress(bookmarkPlace.getRoadAddress())
            .latitude(bookmarkPlace.getLatitude())
            .longitude(bookmarkPlace.getLongitude())
            .build();
    }
}
