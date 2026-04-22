package backend.capstone.domain.dayroute.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record GpsPointBatchUploadResponse(
    @Schema(example = "좌표 업로드에 성공했습니다.") String message
) {

}
