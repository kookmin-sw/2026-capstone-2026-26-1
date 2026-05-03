package backend.capstone.domain.user;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.bookmarkplace.entity.BookmarkPlaceType;
import backend.capstone.domain.bookmarkplace.repository.BookmarkPlaceRepository;
import backend.capstone.domain.user.entity.ProviderType;
import backend.capstone.domain.user.entity.User;
import backend.capstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BookmarkPlaceRepository bookmarkPlaceRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        User testUser1 = User.builder()
            .nickname("테스트유저")
            .provider(ProviderType.KAKAO)
            .providerId("test_kakao_1")
            .build();

        User savedUser = userRepository.save(testUser1);

        BookmarkPlace homeBookmarkPlace = BookmarkPlace.builder()
            .user(savedUser)
            .type(BookmarkPlaceType.HOME)
            .name("파사도 망원")
            .roadAddress("서울 마포구 포은로6길 9")
            .latitude(37.5557871997154)
            .longitude(126.905567731547)
            .build();

        bookmarkPlaceRepository.save(homeBookmarkPlace);
    }
}
