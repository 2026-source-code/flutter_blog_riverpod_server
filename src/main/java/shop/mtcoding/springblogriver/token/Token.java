package shop.mtcoding.springblogriver.token;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import shop.mtcoding.springblogriver.user.User;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "token_tb")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false, length = 500)
    private String accessToken;

    @Column(nullable = false, length = 500)
    private String refreshToken;

    @Column(length = 500)
    private String previousRefreshToken;

    @Column(nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Token(Integer id, User user, String accessToken, String refreshToken,
                 String previousRefreshToken, String deviceId, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.previousRefreshToken = previousRefreshToken;
        this.deviceId = deviceId;
        this.createdAt = createdAt;
    }

    public void rotate(String newAccessToken, String newRefreshToken, String deviceId) {
        this.previousRefreshToken = this.refreshToken;
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
        this.deviceId = deviceId;
    }

    public void updateOnLogin(String accessToken, String refreshToken, String deviceId) {
        this.previousRefreshToken = null;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.deviceId = deviceId;
    }
}
