package backend.capstone.domain.user.entity;

import backend.capstone.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProviderType provider;

    private String providerId;

    private String nickname;

    private String profileImageUrl;

    private LocalTime dayStartTime;

    private LocalTime dayEndTime;

    @Builder
    public User(ProviderType provider, String providerId, String nickname, String profileImageUrl) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.dayStartTime = LocalTime.MIDNIGHT;
        this.dayEndTime = LocalTime.of(23, 59);
    }

    //TODO: 프로필 변경 기능에서 사용
    public void updateProfile(String nickname, String profileImageUrl) {
        if (!Objects.equals(this.nickname, nickname)) {
            this.nickname = nickname;
        }
        if (!Objects.equals(this.profileImageUrl, profileImageUrl)) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}
