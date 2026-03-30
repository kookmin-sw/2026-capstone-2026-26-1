package backend.capstone.domain.dayroute.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.dayroute.dto.DayRouteBookmarkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;

@Tag(name = "나의 지나온길 즐겨찾기 API")
public interface DayRouteBookmarkControllerSpec {

    @Operation(
        summary = "즐겨찾기 토글 API"
    )
    DayRouteBookmarkResponse toggleBookmark(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal
    );
}
