package backend.capstone.domain.dayroute.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMemoRequest;
import backend.capstone.domain.dayroute.dto.DayRouteMemoResponse;
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
import java.time.LocalDate;

@Tag(name = "day route API")
public interface DayRouteControllerSpec {

    @Operation(
        summary = "GPS point upload API",
        description = "date path variable must be passed in yyyy-MM-dd format such as 2026-03-08."
    )
    GpsPointBatchUploadResponse uploadGpsPoints(
        @Parameter(example = "2026-01-01") LocalDate date,
        GpsPointBatchUploadRequest request,
        UserPrincipal principal
    );

    @Operation(
        summary = "day route detail API",
        description = "Places are returned in ascending order by orderIndex."
    )
    DayRouteDetailResponse getDayRouteDetail(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal
    );

    @Operation(
        summary = "memo save API"
    )
    DayRouteMemoResponse saveMemo(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        DayRouteMemoRequest request
    );

    @Operation(
        summary = "title save API"
    )
    DayRouteTitleResponse saveTitle(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        DayRouteTitleRequest request
    );

    @Operation(
        summary = "place add API"
    )
    PlaceAddResponse addPlaceToDayRoute(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        PlaceAddRequest request
    );

    @Operation(
        summary = "place update API",
        description = "Send unchanged values as they are. The request fields fully overwrite the DB values."
    )
    PlaceUpdateResponse updatePlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        @Parameter(example = "1") Long placeId,
        UserPrincipal principal,
        PlaceUpdateRequest request
    );

    @Operation(
        summary = "place delete API"
    )
    void deletePlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        @Parameter(example = "1") Long placeId,
        UserPrincipal principal
    );

    @Operation(
        summary = "place reorder API",
        description = "Receives the full reordered placeId array and updates the place order for the date."
    )
    void reorderPlace(
        @Parameter(example = "2026-01-01") LocalDate date,
        UserPrincipal principal,
        @RequestBody(
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
