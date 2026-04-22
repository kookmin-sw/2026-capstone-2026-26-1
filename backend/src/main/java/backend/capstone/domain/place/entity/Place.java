package backend.capstone.domain.place.entity;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(
    name = "place",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_day_route_order_index", columnNames = {"day_route_id",
            "order_index"})
    }
)
public class Place extends BaseTimeEntity {

    @Id
    @Column(name = "place_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_route_id")
    private DayRoute dayRoute;

    private String roadAddress;

    private String name;

    private double latitude;

    private double longitude;

    private int orderIndex;

    @Enumerated(EnumType.STRING)
    private PlaceSource source;

    @Builder
    Place(DayRoute dayRoute, String roadAddress, String name, double latitude, double longitude,
        int orderIndex, PlaceSource source) {
        this.dayRoute = dayRoute;
        this.roadAddress = roadAddress;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.orderIndex = orderIndex;
        this.source = source;
    }

    public void update(String roadAddress, String name, double latitude, double longitude) {
        this.roadAddress = roadAddress;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void changeOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
