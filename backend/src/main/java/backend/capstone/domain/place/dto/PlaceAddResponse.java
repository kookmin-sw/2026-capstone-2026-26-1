package backend.capstone.domain.place.dto;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.place.entity.PlaceSource;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PlaceAddResponse(
    Long placeId,
    String placeName,
    PlaceSource source,
    BookmarkPlaceType type,
    String roadAddress,
    double latitude,
    double longitude,
    int orderIndex,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
