package backend.capstone.domain.bookmarkplace.entity;

import backend.capstone.domain.user.entity.User;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkPlace extends BaseTimeEntity {

    @Id
    @Column(name = "bookmark_place_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private BookmarkPlaceType type;

    private String name;

    private String roadAddress;

    private double latitude;

    private double longitude;

    @Builder
    public BookmarkPlace(User user, BookmarkPlaceType type, String name, String roadAddress,
        double latitude, double longitude) {
        this.user = user;
        this.type = type;
        this.name = name;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void update(BookmarkPlaceType type, String name, String roadAddress, double latitude,
        double longitude) {
        this.type = type;
        this.name = name;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
