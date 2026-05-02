package backend.capstone.domain.dayroute.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMemoRequest;
import backend.capstone.domain.dayroute.dto.DayRouteMemoResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMonthlyResponse;
import backend.capstone.domain.dayroute.dto.DayRouteSummaryResponse;
import backend.capstone.domain.dayroute.dto.DayRouteTitleRequest;
import backend.capstone.domain.dayroute.dto.DayRouteTitleResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        summary = "하루 요약 조회 API",
        description = """
            해당 날짜의 첫 외출 시간, 마지막 귀가 시간, 총 외출 횟수, 총 외출 시간을 반환합니다.<br>
            외출 또는 귀가 기록이 아직 없거나 집 주소가 등록되지 않으면 outingTime 또는 enterHomeTime은 null로 반환됩니다.<br>
            외출 시간과 귀가 시간은 kst 시간으로 변환되어 반환됩니다.<br>
            totalOutingSeconds는 총 외출 시간을 초 단위로 계산한 값이고<br>
            totalOutingDurationText는 "5시간 30분" (시간이 한자리수일 때 앞에 0 안붙음)과 같이 총 외출 시간을 문자열로 변환하여 반환합니다.
            """
    )
    DayRouteSummaryResponse getDayRouteSummary(
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
        summary = "월별 나의 지나온길 조회 API",
        description = """
            해당 년도, 월의 날짜별 지나온길 데이터들이 반환됩니다.<br>
            검색하고자 하는 년도와 월을 쿼리 파라미터로 넣어서 요청해주세요.<br>
            년도는 2000~3000 사이의 값만 요청할 수 있으며 월은 1~12 사이의 값만 요청할 수 있습니다.<br>
            해당 날짜의 지나온길 데이터가 존재하지 않는 경우 dayRouteExists 필드에 false가 반환되며 dayRoute 필드 또한 null이 반환됩니다.<br>
            hasPolyline은 해당 날짜의 경로 데이터 존재 여부이고, hasDetails는 세부 데이터 존재 여부입니다.
            """
    )
    DayRouteMonthlyResponse getDayRoutesByMonth(
        @Parameter(example = "2026") @Min(2000) @Max(3000) int year,
        @Parameter(example = "1") @Min(1) @Max(12) int month,
        @AuthenticationPrincipal UserPrincipal principal);

}
