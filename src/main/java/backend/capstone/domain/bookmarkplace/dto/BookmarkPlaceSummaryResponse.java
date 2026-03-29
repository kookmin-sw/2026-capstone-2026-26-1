package backend.capstone.domain.bookmarkplace.dto;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record BookmarkPlaceSummaryResponse(

    @Schema(example = "1", description = "즐겨찾기 장소 ID")
    Long placeId,

    @Schema(example = "집", description = "즐겨찾기 장소명")
    String placeName,

    @Schema(example = "HOME", description = "장소 유형")
    BookmarkPlaceType type,

    @Schema(example = "서울 강남구 테헤란로 123", description = "도로명 주소")
    String roadAddress,

    @Schema(example = "37.498095", description = "위도")
    double latitude,

    @Schema(example = "127.027610", description = "경도")
    double longitude
) {

}
