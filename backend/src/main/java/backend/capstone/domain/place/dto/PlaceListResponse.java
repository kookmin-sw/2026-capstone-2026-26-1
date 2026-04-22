package backend.capstone.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record PlaceListResponse(

    @Schema(example = "2", description = "방문 장소 개수")
    int placeCount,

    @Schema(description = "방문 장소 목록")
    List<PlaceItem> places
) {

}
