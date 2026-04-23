package backend.capstone.domain.kakaoplace.controller;

import backend.capstone.domain.kakaoplace.dto.PlaceSearchResponse;
import backend.capstone.domain.kakaoplace.service.KakaoLocalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Tag(name = "카카오 장소 검색 API")
public class KakaoPlaceController {

    private final KakaoLocalSearchService kakaoLocalSearchService;

    @Operation(
        summary = "카카오 장소 검색 API",
        description = """
            장소명으로 카카오 로컬 키워드 검색 API를 호출해 관련 장소 목록을 반환합니다.<br>
            장소는 최대 5개까지 반환합니다.
            """
    )
    @GetMapping("/api/places/search")
    public PlaceSearchResponse searchPlaces(
        @Parameter(example = "국민대학교") @RequestParam @NotBlank String query
    ) {
        return kakaoLocalSearchService.searchPlaces(query);
    }
}
