package backend.capstone.domain.place.dto;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.place.entity.PlaceSource;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PlaceUpdateResponse(
    String roadAddress,
    String placeName,
    PlaceSource source,
    BookmarkPlaceType type,
    double latitude,
    double longitude,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
