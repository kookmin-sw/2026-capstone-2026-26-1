package backend.capstone.domain.bookmarkplace.service;

import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.bookmarkplace.mapper.BookmarkPlaceMapper;
import backend.capstone.domain.bookmarkplace.repository.BookmarkPlaceRepository;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkPlaceService {

    private final BookmarkPlaceRepository bookmarkPlaceRepository;
    private final UserService userService;

    @Transactional
    public BookmarkPlaceCreateResponse createBookmarkPlace(Long userId,
        BookmarkPlaceCreateRequest request) {
        User user = userService.findById(userId);
        BookmarkPlace bookmarkPlace = BookmarkPlaceMapper.toEntity(user, request);
        BookmarkPlace savedBookmarkPlace = bookmarkPlaceRepository.save(bookmarkPlace);

        return BookmarkPlaceMapper.toCreateResponse(savedBookmarkPlace);
    }
}
