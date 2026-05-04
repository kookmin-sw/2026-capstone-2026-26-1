package backend.capstone.domain.bookmarkplace.dto;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import lombok.Builder;

@Builder
public record BookmarkPlaceCreateResponse(
    Long bookmarkPlaceId,
    BookmarkPlaceType type,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude
) {

}
