package backend.capstone.domain.dayroute.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.dayroute.dto.DayRouteBookmarkResponse;
import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMemoRequest;
import backend.capstone.domain.dayroute.dto.DayRouteMemoResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMonthlyResponse;
import backend.capstone.domain.dayroute.dto.DayRouteTitleRequest;
import backend.capstone.domain.dayroute.dto.DayRouteTitleResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceReorderRequest;
import backend.capstone.domain.place.dto.PlaceUpdateRequest;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "나의 지나온길 API")
public interface DayRouteControllerSpec {

    @Operation(
        summary = "좌표, 이동거리 업로드 API",
        description = """
            경로 변수로 들어오는 date는 2026-03-08 같은 형식으로 넣어주세요.<br>
            최대한 실시간성을 유지하기 위해 주기적으로 좌표를 업로드해주세요.<br>
            이동거리는 km 단위이며 해당 날짜 기준 가장 마지막으로 들어온 값이 최종 이동거리가 됩니다.<br>
            만약 새롭게 업로드할 좌표가 들어오지 않았다면 api를 호출하지 않아도 됩니다.
            """
    )
    GpsPointBatchUploadResponse uploadGpsPoints(
        @Parameter(example = "2026-01-01") LocalDate date,
        @Valid GpsPointBatchUploadRequest request,
        UserPrincipal principal
    );

    @Operation(
        summary = "나의 지나온길 조회 API",
        description = """
            해당 날짜의 좌표, 수기 장소, 메모, 제목 등 지나온길의 모든 데이터들이 반환됩니다.<br>
            encodedPath는 해당 날짜의 지나온길 좌표들을 인코딩한 값입니다.<br>
            pathPointCount는 좌표 개수를 의미합니다. <br>
            place는 orderIndex를 기준으로 오름차순 정렬되어 반환됩니다.
            """
    )
    DayRouteDetailResponse getDayRouteDetail(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal
    );

    @Operation(
        summary = "메모 작성 및 수정 API",
        description = """
            메모는 따로 수정, 삭제 api가 존재하지 않습니다. 이 api의 요청값으로 db의 메모 값이 그대로 덮어써집니다.<br>
            만약 값으로 null, 빈 문자열(""), 공백 문자열("   ") 등이 들어온다면 삭제로 간주하여 해당 날짜의 메모가 null로 저장됩니다.<br>
            """
    )
    DayRouteMemoResponse replaceMemo(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        DayRouteMemoRequest request
    );

    @Operation(
        summary = "제목 작성 및 수정 API",
        description = """
            제목은 따로 수정, 삭제 api가 존재하지 않습니다. 이 api의 요청값으로 db의 제목 값이 그대로 덮어써집니다.<br>
            만약 값으로 null, 빈 문자열(""), 공백 문자열("   ") 등이 들어온다면 삭제로 간주하여 해당 날짜의 제목이 null로 저장됩니다.<br>
            """
    )
    DayRouteTitleResponse replaceTitle(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        DayRouteTitleRequest request
    );

    @Operation(
        summary = "즐겨찾기 토글 API"
    )
    DayRouteBookmarkResponse toggleBookmark(
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
            수정하지 않은 필드도 그대로 넣어주세요. 요청 필드 값으로 DB 값이 그대로 덮어써집니다.(put mapping임)
            """
    )
    PlaceUpdateResponse updatePlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        @Parameter(example = "1") Long placeId,
        UserPrincipal principal,
        PlaceUpdateRequest request
    );

    @Operation(
        summary = "장소 삭제 API"
    )
    void deletePlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        @Parameter(example = "1") Long placeId,
        UserPrincipal principal
    );

    @Operation(
        summary = "월별 나의 지나온길 조회 API",
        description = """
            해당 년도, 월의 날짜별 지나온길 데이터들이 반환됩니다.<br>
            검색하고자 하는 년도와 월을 쿼리 파라미터로 넣어서 요청해주세요.<br>
            년도는 2000~3000 사이의 값만 요청할 수 있으며 월은 1~12 사이의 값만 요청할 수 있습니다.<br>
            해당 날짜의 지나온길 데이터가 존재하지 않는 경우 dayRouteExists 필드에 false가 반환되며 dayRoute 필드 또한 null이 반환됩니다.<br>
            hasGpsPoints는 해당 날짜의 위치 데이터 존재 여부이고, hasManualData는 수기 데이터 존재 여부입니다.
            """
    )
    DayRouteMonthlyResponse getDayRoutesByMonth(
        @Parameter(example = "2026") @Min(2000) @Max(3000) int year,
        @Parameter(example = "1") @Min(1) @Max(12) int month,
        @AuthenticationPrincipal UserPrincipal principal);

    @Operation(
        summary = "장소 순서 변경 API",
        description = """
            정렬된 placeId 배열 전체를 받아 해당 날짜의 장소 순서를 일괄 변경합니다.
            """
    )
    void reorderPlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        @Valid @RequestBody(
            content = @Content(
                schema = @Schema(implementation = PlaceReorderRequest.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "placeIds": [2,1]
                        }
                        """
                )
            )
        ) PlaceReorderRequest request
    );
}
