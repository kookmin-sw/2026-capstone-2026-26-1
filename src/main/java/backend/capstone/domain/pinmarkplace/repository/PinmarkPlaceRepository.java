package backend.capstone.domain.pinmarkplace.repository;

import backend.capstone.domain.pinmarkplace.entity.PinmarkPlace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinmarkPlaceRepository extends JpaRepository<PinmarkPlace, Long> {

}
