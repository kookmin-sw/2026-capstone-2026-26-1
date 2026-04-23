package backend.capstone.domain.place.dto;

import backend.capstone.domain.place.entity.PlaceSource;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record PlaceItem(

    @Schema(example = "1", description = "방문 장소 ID")
    Long placeId,

    @Schema(example = "스타벅스 강남점", description = "장소명")
    String placeName,

    @Schema(example = "AUTO", description = "장소 생성 유형")
    PlaceSource source,

    @Schema(example = "서울 강남구 테헤란로 123", description = "도로명 주소")
    String roadAddress,

    @Schema(example = "37.498095", description = "위도")
    double latitude,

    @Schema(example = "127.027610", description = "경도")
    double longitude,

    @Schema(example = "1", description = "일정 내 장소 순서")
    int orderIndex,

    @Schema(example = "09:30:00", description = "장소 체류 시작 시간")
    LocalDateTime startTime,

    @Schema(example = "10:15:00", description = "장소 체류 종료 시간")
    LocalDateTime endTime
) {
}
