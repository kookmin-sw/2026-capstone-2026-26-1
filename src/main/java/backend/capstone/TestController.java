package backend.capstone;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserService userService;

    @Operation(summary = "테스트용 엔드포인트",
        description = """
            엑세스 토큰을 이용한 인증이 잘되고 있는지 확인하는 테스트용 엔드포인트입니다.<br>
            헤더에 엑세스 토큰을 넣어서 요청했을 때 200 상태코드와 함께 "${nickname}님 환영합니다." 라는 문자열이 정상적으로 반환되는지 확인해보세요.<br>
            엑세스 토큰 만료 메시지를 받으면 토큰 재발급 API를 호출해주세요.
            """)
    @GetMapping("/test")
    public String test(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.findById(principal.userId()).getNickname() + "님 환영합니다!";
    }

}
