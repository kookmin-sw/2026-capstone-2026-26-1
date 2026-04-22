package backend.capstone.domain.bookmarkplace.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceListResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceUpdateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "즐겨찾기 장소 API")
public interface BookmarkPlaceControllerSpec {

    @Operation(
        summary = "즐겨찾기 장소 목록 조회 API",
        description = """
            사용자의 즐겨찾기 장소 목록과 전체 개수를 조회합니다.<br>
            즐겨찾기 장소가 없는 경우, placeCount는 0, bookmarkPlaces는 빈 배열이 반환됩니다.
            """
    )
    BookmarkPlaceListResponse getBookmarkPlaces(UserPrincipal principal);

    @Operation(
        summary = "즐겨찾기 장소 생성 API",
        description = """
            type 필드의 값은 HOME/COMPANY/SCHOOL/ETC 중 하나를 선택해 주세요.
            """
    )
    BookmarkPlaceCreateResponse createBookmarkPlace(
        UserPrincipal principal,
        BookmarkPlaceCreateRequest request
    );

    @Operation(
        summary = "즐겨찾기 장소 수정 API",
        description = """
            type 필드의 값은 HOME/COMPANY/SCHOOL/ETC 중 하나를 선택해 주세요.<br>
            해당 api는 put 메서드로 해당 BookmarkPlace의 모든 필드들을 덮어씌워서 저장합니다. 변경되지 않은 필드도 원래값들을 넣어주세요.
            """
    )
    BookmarkPlaceUpdateResponse updateBookmarkPlace(
        UserPrincipal principal,
        @Parameter(example = "1") Long bookmarkPlaceId,
        BookmarkPlaceUpdateRequest request
    );

    @Operation(
        summary = "즐겨찾기 장소 삭제 API",
        description = """
            본인의 즐겨찾기 장소만 삭제할 수 있습니다.
            """
    )
    void deleteBookmarkPlace(
        UserPrincipal principal,
        @Parameter(example = "1") Long bookmarkPlaceId
    );
}
