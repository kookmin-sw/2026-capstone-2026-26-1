package backend.capstone.domain.dayroute.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.facade.DayRouteFacade;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceUpdateRequest;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/day-routes")
public class DayRouteController implements DayRouteControllerSpec {

    private final DayRouteFacade dayRouteFacade;

    @Override
    @PostMapping("/{date}/gps-points:batch")
    public GpsPointBatchUploadResponse uploadGpsPoints(
        @PathVariable LocalDate date,
        @Valid @RequestBody GpsPointBatchUploadRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dayRouteFacade.uploadGpsPoint(date, principal.userId(), request);
    }

//    @Override
//    @GetMapping("/{date}/gps-points")
//    public GpsPointsResponse getGpsPoints(
//        @PathVariable LocalDate date,
//        @AuthenticationPrincipal UserPrincipal principal
//    ) {
//        return dayRouteFacade.getGpsPoints(date, principal.userId());
//    }

    @Override
    @GetMapping("/{date}")
    public DayRouteDetailResponse getDayRouteDetail(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dayRouteFacade.getDayRouteDetail(date, principal.userId());
    }

    @Override
    @PostMapping("/{date}/places")
    public PlaceAddResponse addPlaceToDayRoute(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody PlaceAddRequest request
    ) {
        return dayRouteFacade.addPlaceToDayRoute(date, principal.userId(), request);
    }

    @PutMapping("/{date}/places/{placeId}")
    public PlaceUpdateResponse updatePlace(
        @PathVariable LocalDate date,
        @PathVariable Long placeId,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody PlaceUpdateRequest request
    ) {
        return dayRouteFacade.updatePlace(date, principal.userId(), placeId, request);
    }

    @Override
    @DeleteMapping("/{date}/places/{placeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlace(
        @PathVariable LocalDate date,
        @PathVariable Long placeId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        dayRouteFacade.deletePlace(date, principal.userId(), placeId);
    }

}
