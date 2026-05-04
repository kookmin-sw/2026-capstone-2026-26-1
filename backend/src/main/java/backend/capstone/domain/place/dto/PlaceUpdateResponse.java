package backend.capstone.domain.place.dto;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.place.entity.PlaceSource;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record PlaceUpdateResponse(
    String roadAddress,
    String placeName,
    PlaceSource source,
    BookmarkPlaceType type,
    double latitude,
    double longitude,
    @Schema(example = "2026-04-29T13:32:43.059+09:00")
    OffsetDateTime startTime,
    @Schema(example = "2026-04-29T13:32:43.059+09:00")
    OffsetDateTime endTime
) {
}
