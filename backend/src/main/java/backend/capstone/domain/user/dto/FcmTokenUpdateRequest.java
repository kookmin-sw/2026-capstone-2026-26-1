package backend.capstone.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenUpdateRequest(
    @NotBlank String fcmToken
) {

}
