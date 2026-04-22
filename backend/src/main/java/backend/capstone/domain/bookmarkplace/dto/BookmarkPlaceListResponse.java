package backend.capstone.domain.bookmarkplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record BookmarkPlaceListResponse(
    @Schema(example = "1", description = "즐겨찾기 장소 개수")
    int placeCount,

    @Schema(description = "즐겨찾기 장소 목록")
    List<BookmarkPlaceSummaryResponse> bookmarkPlaces
) {

}
