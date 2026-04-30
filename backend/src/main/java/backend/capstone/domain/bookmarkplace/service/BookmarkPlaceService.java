package backend.capstone.domain.bookmarkplace.service;

import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceListResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceUpdateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceUpdateResponse;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.bookmarkplace.exception.BookmarkPlaceErrorCode;
import backend.capstone.domain.bookmarkplace.mapper.BookmarkPlaceMapper;
import backend.capstone.domain.bookmarkplace.repository.BookmarkPlaceRepository;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.service.UserService;
import backend.capstone.global.exception.BusinessException;
import java.util.List;
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
        validateHomeBookmarkPlaceCreation(userId, request.type());
        User user = userService.findById(userId);
        BookmarkPlace bookmarkPlace = BookmarkPlaceMapper.toEntity(user, request);
        BookmarkPlace savedBookmarkPlace = bookmarkPlaceRepository.save(bookmarkPlace);

        return BookmarkPlaceMapper.toCreateResponse(savedBookmarkPlace);
    }

    @Transactional
    public BookmarkPlaceUpdateResponse updateBookmarkPlace(Long userId, Long bookmarkPlaceId,
        BookmarkPlaceUpdateRequest request) {
        BookmarkPlace bookmarkPlace = bookmarkPlaceRepository.findByIdAndUserId(bookmarkPlaceId,
                userId)
            .orElseThrow(
                () -> new BusinessException(BookmarkPlaceErrorCode.BOOKMARK_PLACE_NOT_FOUND));

        validateHomeBookmarkPlaceUpdate(userId, bookmarkPlace, request.type());

        bookmarkPlace.update(request.type(), request.placeName(), request.roadAddress(),
            request.latitude(), request.longitude()
        );

        return BookmarkPlaceMapper.toUpdateResponse(bookmarkPlace);
    }

    @Transactional
    public void deleteBookmarkPlace(Long userId, Long bookmarkPlaceId) {
        BookmarkPlace bookmarkPlace = bookmarkPlaceRepository.findByIdAndUserId(bookmarkPlaceId,
                userId)
            .orElseThrow(
                () -> new BusinessException(BookmarkPlaceErrorCode.BOOKMARK_PLACE_NOT_FOUND));

        bookmarkPlaceRepository.delete(bookmarkPlace);
    }

    @Transactional(readOnly = true)
    public BookmarkPlaceListResponse getBookmarkPlaces(Long userId) {
        List<BookmarkPlace> bookmarkPlaces = bookmarkPlaceRepository.findByUserIdOrderByIdAsc(
            userId);

        return BookmarkPlaceMapper.toListResponse(bookmarkPlaces);
    }

    @Transactional(readOnly = true)
    public List<BookmarkPlace> getBookmarkPlaceByUserId(Long userId) {
        return bookmarkPlaceRepository.findByUserIdOrderByIdAsc(userId);
    }

    private void validateHomeBookmarkPlaceCreation(Long userId, BookmarkPlaceType type) {
        if (type == BookmarkPlaceType.HOME
            && bookmarkPlaceRepository.existsByUserIdAndType(userId, BookmarkPlaceType.HOME)) {
            throw new BusinessException(BookmarkPlaceErrorCode.HOME_BOOKMARK_PLACE_ALREADY_EXISTS);
        }
    }

    private void validateHomeBookmarkPlaceUpdate(Long userId, BookmarkPlace bookmarkPlace,
        BookmarkPlaceType requestedType) {
        if (requestedType == BookmarkPlaceType.HOME
            && bookmarkPlace.getType() != BookmarkPlaceType.HOME
            && bookmarkPlaceRepository.existsByUserIdAndType(userId, BookmarkPlaceType.HOME)) {
            throw new BusinessException(BookmarkPlaceErrorCode.HOME_BOOKMARK_PLACE_ALREADY_EXISTS);
        }
    }
}
