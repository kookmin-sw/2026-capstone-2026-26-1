package backend.capstone.domain.dayroute.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record DayRouteSummaryResponse(
    @Schema(example = "2026-04-29T08:10:00+09:00", description = "해당 날짜의 첫 외출 시간")
    OffsetDateTime outingTime,

    @Schema(example = "2026-04-29T18:40:00+09:00", description = "해당 날짜의 마지막 귀가 시간")
    OffsetDateTime enterHomeTime,

    @Schema(example = "2", description = "해당 날짜의 총 외출 횟수")
    int totalOutingCount,

    @Schema(example = "36120", description = "해당 날짜의 총 외출 시간")
    long totalOutingSeconds
) {

}
