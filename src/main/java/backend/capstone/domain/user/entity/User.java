package backend.capstone.domain.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private ProviderType provider;

	private String providerId;

	private String nickname;

	private String profileImageUrl;

	@Builder
	public User(ProviderType provider, String providerId, String nickname, String profileImageUrl) {
		this.provider = provider;
		this.providerId = providerId;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
	}

	public void updateProfile(String nickname, String profileImageUrl) {
		if (!Objects.equals(this.nickname, nickname)) {
			this.nickname = nickname;
		}
		if (!Objects.equals(this.profileImageUrl, profileImageUrl)) {
			this.profileImageUrl = profileImageUrl;
		}
	}
}
