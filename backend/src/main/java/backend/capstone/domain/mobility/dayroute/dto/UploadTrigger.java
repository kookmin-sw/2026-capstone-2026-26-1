package backend.capstone.domain.mobility.dayroute.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

// 안드로이드가 GPS 배치 업로드를 어떤 이유로 트리거했는지 나타내는 관측용 값.
// 클라이언트가 아직 서버가 모르는 새 트리거 값을 보내더라도 업로드 자체는 실패하지 않도록,
// 인식 못한 값은 UNKNOWN으로 받는다(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE 필요).
public enum UploadTrigger {
    WATCHING_IMMEDIATE,
    BATCH_SIZE,
    PERIODIC,
    PRE_BOUNDARY,
    NETWORK_RECOVERY,
    STOP_FLUSH,
    @JsonEnumDefaultValue
    UNKNOWN
}
