package backend.capstone.domain.bookmarkplace.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "즐찾 장소 API")
public interface BookmarkPlaceControllerSpec {

    @Operation(summary = "즐찾 장소 생성 API",
        description = """
            type 필드의 값은 HOME/COMPANY/SCHOOL/ETC 중에 하나를 선택해주세요.
            """)
    BookmarkPlaceCreateResponse createBookmarkPlace(
        UserPrincipal principal,
        BookmarkPlaceCreateRequest request);
}
