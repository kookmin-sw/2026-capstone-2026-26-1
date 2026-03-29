package backend.capstone.domain.place.repository;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.place.entity.Place;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("""
        select coalesce(max(rp.orderIndex), 0)
        from Place rp
        where rp.dayRoute = :dayRoute
        """)
    int findMaxOrderIdxByRoute(@Param("dayRoute") DayRoute dayRoute);

    List<Place> findByDayRouteOrderByOrderIndex(DayRoute dayRoute);

    boolean existsByDayRoute(DayRoute dayRoute);

    Optional<Place> findByIdAndDayRoute(Long placeId, DayRoute dayRoute);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Place p
        set p.orderIndex = p.orderIndex - 1
        where p.dayRoute = :dayRoute
          and p.orderIndex > :orderIndex
        """)
    int decrementOrderIndexesGreaterThan(@Param("dayRoute") DayRoute dayRoute,
        @Param("orderIndex") int orderIndex);

}
