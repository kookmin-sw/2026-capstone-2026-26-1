package backend.capstone.domain.place.dto;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.place.entity.PlaceSource;
import java.time.Instant;
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
    Instant startTime,
    Instant endTime
) {
}
