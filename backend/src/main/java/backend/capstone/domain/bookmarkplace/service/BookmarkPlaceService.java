package backend.capstone.domain.bookmarkplace.service;

import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceCreateResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceListResponse;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceUpdateRequest;
import backend.capstone.domain.bookmarkplace.dto.BookmarkPlaceUpdateResponse;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.bookmarkplace.exception.BookmarkPlaceErrorCode;
import backend.capstone.domain.bookmarkplace.mapper.BookmarkPlaceMapper;
import backend.capstone.domain.bookmarkplace.repository.BookmarkPlaceRepository;
import backend.capstone.domain.place.service.KakaoCoordinateConversionService;
import backend.capstone.domain.place.service.dto.Wgs84Coordinate;
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
    private final KakaoCoordinateConversionService kakaoCoordinateConversionService;

    @Transactional
    public BookmarkPlaceCreateResponse createBookmarkPlace(Long userId,
        BookmarkPlaceCreateRequest request) {
        User user = userService.findById(userId);
        Wgs84Coordinate coordinate = kakaoCoordinateConversionService.convertTm128ToWgs84(
            request.longitude(),
            request.latitude()
        );
        BookmarkPlace bookmarkPlace = BookmarkPlaceMapper.toEntity(user, request, coordinate);
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
}
