package backend.capstone;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Operation(summary = "테스트용 엔드포인트",
        description = """
            서버가 정상적으로 작동하는지 확인하는 테스트용 엔드포인트입니다.
            헤더에 엑세스 토큰을 넣어서 요청했을 때 200 상태코드와 함께 "test"문자열이 정상적으로 반환되는지 확인해보세요.
            엑세스 토큰 만료 메시지를 받으면 재발급 API를 호출해주세요.
            """)
    @GetMapping("/test")
    public String test() {
        return "test";
    }

}
