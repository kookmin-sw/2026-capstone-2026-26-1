package backend.capstone.domain.mobility.dayroute.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.Instant;
import java.util.List;

public record GpsPointBatchUploadRequest(
//    String deviceId,
    double distance,
    List<GpsPointRequest> gpsPoints,
    // 업로드가 어떤 이유로 트리거됐는지 나타내는 관측용 값(예: WATCHING_IMMEDIATE, PERIODIC).
    // 미지의 값이나 필드 누락에도 역직렬화가 깨지지 않도록 enum이 아닌 String으로 받는다.
    String uploadTrigger
) {

    // 기존 2-arg 호출부(테스트 등)를 위한 보조 생성자.
    public GpsPointBatchUploadRequest(double distance, List<GpsPointRequest> gpsPoints) {
        this(distance, gpsPoints, null);
    }

    public record GpsPointRequest(
        Instant recordedAt,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") double latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") double longitude
    ) {

    }
}
