package backend.capstone.domain.user.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.user.dto.FcmTokenUpdateRequest;
import backend.capstone.domain.user.dto.FcmTokenUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "사용자 API")
public interface UserControllerSpec {

    @Operation(
        summary = "FCM 토큰 등록/갱신 API",
        description = """
            로그인 직후 또는 FCM 토큰이 갱신될 때마다 호출해주세요.<br>
            이 토큰은 보호자가 위치 조회 화면을 보는 동안 위치 전송 주기를 단축하라는 푸시 알림을 보낼 때 사용됩니다.
            """
    )
    FcmTokenUpdateResponse updateFcmToken(
        @Valid FcmTokenUpdateRequest request,
        UserPrincipal principal
    );
}
