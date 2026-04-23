package backend.capstone.domain.kakaoplace.controller;

import backend.capstone.domain.kakaoplace.dto.PlaceSearchResponse;
import backend.capstone.domain.kakaoplace.service.KakaoLocalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Validated
@Tag(name = "카카오 장소 검색 API")
public class KakaoPlaceController {

    private final KakaoLocalSearchService kakaoLocalSearchService;

    @Operation(
        summary = "카카오 장소 검색 API",
        description = """
            검색어로 카카오 장소 검색 API를 호출해 장소 목록을 반환합니다.<br>
            <br>
            - `page`는 현재 요청한 페이지 번호입니다.<br>
            - `size`는 한 번에 요청한 개수입니다.<br>
            - `places`는 실제 이번 응답에서 내려온 장소 목록입니다.<br>
            - `isEnd=true`이면 검색 결과를 모두 응답받았기에 다음 페이지를 요청하지 않아도 됩니다.<br>
            - `pageableCount`는 카카오 API 기준으로 조회 가능한 전체 결과 수입니다.<br>
            <br>
            - 무한스크롤 구현 시:<br>
              - 첫 요청은 `page=1`<br>
              - 다음 요청은 `page + 1`<br>
              - `isEnd=true`가 오면 추가 요청 중단
            """
    )
    @GetMapping("/api/places/search")
    public PlaceSearchResponse searchPlaces(
        @Parameter(example = "국민대학교") @RequestParam @NotBlank String query,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return kakaoLocalSearchService.searchPlaces(query, page, size);
    }
}
