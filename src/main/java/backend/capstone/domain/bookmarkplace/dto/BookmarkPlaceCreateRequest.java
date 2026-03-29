package backend.capstone.domain.bookmarkplace.dto;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;

public record BookmarkPlaceCreateRequest(
    BookmarkPlaceType type,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude
) {

}
