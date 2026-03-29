package backend.capstone.domain.bookmarkplace.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "북마크 장소 API")
public interface BookmarkPlaceControllerSpec {

    @Operation(summary = "북마크 장소 생성 API",
        description = """
            type은 HOME/COMPANY/SCHOOL 중에 하나를 선택해주세요.
            """)
    BookmarkPlaceCreateResponse createBookmarkPlace(
        UserPrincipal principal,
        BookmarkPlaceCreateRequest request);
}
