package backend.capstone.domain.dayroute.entity;

public enum HomeRouteStatus {
    UNKNOWN, //아직 판정 전
    NO_HOME_BOOKMARK, //사용자가 집주소를 등록하지 않음
    AT_HOME, //현재 집 안 상태로 판정됨
    OUTING, //외출 상태로 판정됨
    RETURNED_HOME //외출 후 다시 귀가한 상태로 판정됨
}
