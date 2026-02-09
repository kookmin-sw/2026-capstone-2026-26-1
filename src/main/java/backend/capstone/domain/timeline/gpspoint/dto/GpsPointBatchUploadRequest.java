package backend.capstone.domain.timeline.gpspoint.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.LocalDateTime;
import java.util.List;

public record GpsPointBatchUploadRequest(
//    String deviceId,
    List<GpsPointRequest> gpsPoints //TODO: 최대 사이즈 제한
) {

    public record GpsPointRequest(
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") double latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") double longitude,
        LocalDateTime recordedAt
    ) {

    }
}
