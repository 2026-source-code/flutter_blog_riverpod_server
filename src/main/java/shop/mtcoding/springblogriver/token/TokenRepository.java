package shop.mtcoding.springblogriver.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByRefreshToken(String refreshToken);

    Optional<Token> findByPreviousRefreshToken(String previousRefreshToken);

    @Query("SELECT t FROM Token t WHERE t.user.id = :userId")
    Optional<Token> findByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE Token t SET t.accessToken = :accessToken, t.refreshToken = :newRefreshToken, t.previousRefreshToken = :prevRefreshToken, t.deviceId = :deviceId WHERE t.id = :id")
    void rotateById(@Param("id") Integer id,
                    @Param("accessToken") String accessToken,
                    @Param("newRefreshToken") String newRefreshToken,
                    @Param("prevRefreshToken") String prevRefreshToken,
                    @Param("deviceId") String deviceId);
}
