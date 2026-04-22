package backend.capstone.domain.place.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceListResponse;
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
import java.time.LocalDate;

@Tag(name = "방문 장소 API")
public interface PlaceControllerSpec {

    @Operation(
        summary = "방문 장소 목록 조회 API",
        description = """
            해당 날짜의 방문 장소 목록을 조회합니다.<br>
            장소 목록은 orderIndex 오름차순으로 반환됩니다.<br>
            tpe 필드는 장소 생성 유형을 나타냅니다.<br>
             - AUTO: 자동 생성된 장소 (GPS 기반)<br>
             - MANUAL: 사용자가 직접 추가한 장소
            """
    )
    PlaceListResponse getPlaces(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal
    );

    @Operation(
        summary = "방문 장소 등록 API"
    )
    PlaceAddResponse addPlaceToDayRoute(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        PlaceAddRequest request
    );

    @Operation(
        summary = "방문 장소 수정 API",
        description = """
            수정하지 않는 필드도 함께 요청값으로 전달해야 합니다.<br>
            PUT 요청으로 장소 정보를 전체 교체합니다.<br>
            """
    )
    PlaceUpdateResponse updatePlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        @Parameter(example = "1") Long placeId,
        UserPrincipal principal,
        PlaceUpdateRequest request
    );

    @Operation(
        summary = "방문 장소 삭제 API"
    )
    void deletePlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        @Parameter(example = "1") Long placeId,
        UserPrincipal principal
    );

    @Operation(
        summary = "방문 장소 순서 변경 API",
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
                          "placeIds": [2, 1]
                        }
                        """
                )
            )
        ) PlaceReorderRequest request
    );
}
