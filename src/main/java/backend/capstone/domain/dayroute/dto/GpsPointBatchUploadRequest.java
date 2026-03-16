package backend.capstone.domain.dayroute.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.LocalDateTime;
import java.util.List;

public record GpsPointBatchUploadRequest(
//    String deviceId,
    List<GpsPointRequest> gpsPoints
) {

    public record GpsPointRequest(
        LocalDateTime recordedAt,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") double latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") double longitude
    ) {

    }
}
