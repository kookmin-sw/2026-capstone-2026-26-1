package backend.capstone.domain.bookmarkplace.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import backend.capstone.domain.bookmarkplace.service.BookmarkPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmark-places")
public class BookmarkPlaceController implements BookmarkPlaceControllerSpec {

    private final BookmarkPlaceService bookmarkPlaceService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkPlaceCreateResponse createBookmarkPlace(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody BookmarkPlaceCreateRequest request
    ) {
        return bookmarkPlaceService.createBookmarkPlace(principal.userId(), request);
    }
}
