package backend.capstone.domain.dayroute.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.dto.GpsPointsResponse;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceUpdateRequest;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;

@Tag(name = "일차 경로 관련 API")
public interface DayRouteControllerSpec {

    @Operation(
        summary = "좌표 일괄 업로드 API",
        description = "경로 변수로 넣어주는 date는 2026-03-08 같은 형식으로 넣어주세요<br>"
    )
    GpsPointBatchUploadResponse uploadGpsPoints(
        @Parameter(example = "2026-01-01") LocalDate date,
        GpsPointBatchUploadRequest request,
        UserPrincipal principal
    );

    @Operation(
        summary = "좌표 목록 조회 API",
        description = """
            해당 일차의 좌표 목록을 조회합니다.
            """
    )
    GpsPointsResponse getGpsPoints(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal
    );

    @Operation(
        summary = "좌표를 제외한 해당 일차의 데이터 조회 API",
        description = "place의 orderIndex는 장소들의 순서이며 이 순서대로 오름차순 정렬해서 데이터가 반환됩니다."
    )
    DayRouteDetailResponse getDayRouteDetail(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal
    );

    @Operation(
        summary = "장소 등록 API"
    )
    PlaceAddResponse addPlaceToDayRoute(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        PlaceAddRequest request
    );

    @Operation(
        summary = "장소 수정 API",
        description = """
            수정되지 않은 정보도 값으로 넣어주세요. 해당 필드들은 DB에서 통째로 업데이트됩니다.
            """
    )
    PlaceUpdateResponse updatePlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        @Parameter(example = "1") Long placeId,
        UserPrincipal principal,
        PlaceUpdateRequest request
    );


}
