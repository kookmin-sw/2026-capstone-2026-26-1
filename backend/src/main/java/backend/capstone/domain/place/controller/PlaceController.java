package backend.capstone.domain.place.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceListResponse;
import backend.capstone.domain.place.dto.PlaceReorderRequest;
import backend.capstone.domain.place.dto.PlaceSearchResponse;
import backend.capstone.domain.place.dto.PlaceUpdateRequest;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import backend.capstone.domain.place.facade.PlaceFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class PlaceController implements PlaceControllerSpec {

    private final PlaceFacade placeFacade;

    @Override
    @GetMapping("/api/day-routes/{date}/places")
    public PlaceListResponse getPlaces(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return placeFacade.getPlaces(date, principal.userId());
    }

    @Override
    @PostMapping("/api/day-routes/{date}/places")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceAddResponse addPlaceToDayRoute(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody PlaceAddRequest request
    ) {
        return placeFacade.addPlace(date, principal.userId(), request);
    }

    @Override
    @PutMapping("/api/day-routes/{date}/places/{placeId}")
    public PlaceUpdateResponse updatePlace(
        @PathVariable LocalDate date,
        @PathVariable Long placeId,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody PlaceUpdateRequest request
    ) {
        return placeFacade.updatePlace(date, principal.userId(), placeId, request);
    }

    @Override
    @DeleteMapping("/api/day-routes/{date}/places/{placeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlace(
        @PathVariable LocalDate date,
        @PathVariable Long placeId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        placeFacade.deletePlace(date, principal.userId(), placeId);
    }

    @Override
    @PutMapping("/api/day-routes/{date}/places:reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderPlace(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody PlaceReorderRequest request
    ) {
        placeFacade.reorderPlace(date, principal.userId(), request);
    }

    @Override
    @GetMapping("/api/places/search")
    public PlaceSearchResponse searchPlaces(
        @RequestParam @NotBlank String query
    ) {
        return placeFacade.searchPlaces(query);
    }
}
