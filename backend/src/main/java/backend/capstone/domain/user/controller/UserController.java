package backend.capstone.domain.user.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.user.dto.FcmTokenUpdateRequest;
import backend.capstone.domain.user.dto.FcmTokenUpdateResponse;
import backend.capstone.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController implements UserControllerSpec {

    private final UserService userService;

    @Override
    @PatchMapping("/me/fcm-token")
    public FcmTokenUpdateResponse updateFcmToken(
        @Valid @RequestBody FcmTokenUpdateRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.updateFcmToken(principal.userId(), request.fcmToken());
        return new FcmTokenUpdateResponse("FCM 토큰이 등록되었습니다.");
    }
}
