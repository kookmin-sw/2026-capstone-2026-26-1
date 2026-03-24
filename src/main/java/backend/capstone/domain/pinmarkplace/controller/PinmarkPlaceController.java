package backend.capstone.domain.pinmarkplace.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.pinmarkplace.dto.PinmarkPlaceCreateRequest;
import backend.capstone.domain.pinmarkplace.dto.PinmarkPlaceCreateResponse;
import backend.capstone.domain.pinmarkplace.service.PinmarkPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pinmark-places")
public class PinmarkPlaceController implements PinmarkPlaceControllerSpec {

    private final PinmarkPlaceService pinmarkPlaceService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PinmarkPlaceCreateResponse createPinmarkPlace(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody PinmarkPlaceCreateRequest request
    ) {
        return pinmarkPlaceService.createPinmarkPlace(principal.userId(), request);
    }
}
