package backend.capstone.domain.timeline.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record GpsPointBatchUploadRequest(
//    String deviceId,
    @Size(max = 250) List<GpsPointRequest> gpsPoints
) {

    public record GpsPointRequest(
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") double latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") double longitude,
        LocalDateTime recordedAt
    ) {

    }
}
