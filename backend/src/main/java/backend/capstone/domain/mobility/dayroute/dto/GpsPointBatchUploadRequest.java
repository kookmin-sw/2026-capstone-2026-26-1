package backend.capstone.domain.mobility.dayroute.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.Instant;
import java.util.List;

public record GpsPointBatchUploadRequest(
//    String deviceId,
    double distance,
    List<GpsPointRequest> gpsPoints,
    // 업로드가 어떤 이유로 트리거됐는지 나타내는 관측용 값(예: WATCHING_IMMEDIATE, PERIODIC).
    // 인식 못한 값은 UploadTrigger.UNKNOWN으로 받아, 이 필드 하나 때문에 업로드 전체가
    // 실패하지 않게 한다.
    @JsonFormat(with = JsonFormat.Feature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
    UploadTrigger uploadTrigger
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
