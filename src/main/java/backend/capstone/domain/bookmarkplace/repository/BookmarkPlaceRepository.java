package backend.capstone.domain.bookmarkplace.repository;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkPlaceRepository extends JpaRepository<BookmarkPlace, Long> {

}
