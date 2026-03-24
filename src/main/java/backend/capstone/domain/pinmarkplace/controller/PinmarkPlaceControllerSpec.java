package backend.capstone.domain.pinmarkplace.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.pinmarkplace.dto.PinmarkPlaceCreateRequest;
import backend.capstone.domain.pinmarkplace.dto.PinmarkPlaceCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "핀마크 장소 API")
public interface PinmarkPlaceControllerSpec {

    @Operation(summary = "핀마크 장소 생성 API",
        description = """
            type은 HOME/COMPANY/SCHOOL 중에 하나를 넣어주세요.
            """)
    PinmarkPlaceCreateResponse createPinmarkPlace(
        UserPrincipal principal,
        PinmarkPlaceCreateRequest request
    );
}
