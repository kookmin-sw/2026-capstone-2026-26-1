package backend.capstone.domain.gpspoint.repository;

import backend.capstone.domain.gpspoint.entity.GpsPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsPointRepository extends JpaRepository<GpsPoint, Long> {

}
