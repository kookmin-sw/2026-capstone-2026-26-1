package backend.capstone.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

public record LoginResponse(
    Long userId,
    String nickname,
    String profileImageUrl,

    @Schema(type = "string", example = "00:00", description = "시작 시간 (HH:mm)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime dayStartTime,

    @Schema(type = "string", example = "23:59", description = "종료 시간 (HH:mm)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime dayEndTime,

    String accessToken,
    String refreshToken
) {

}
