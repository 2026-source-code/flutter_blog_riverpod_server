package shop.mtcoding.springblogriver._core.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import shop.mtcoding.springblogriver.user.User;

import java.time.Instant;
import java.util.UUID;

public class JwtUtil {

    // 액세스 토큰: 30분
    public final static Long EXPIRATION_TIME = 1000L * 60 * 30;

    // 리프레시 토큰: 7일
    public final static Long EXPIRATION_REFRESH_TIME = 1000L * 60 * 60 * 24 * 7;

    public static String createdAccessToken(User user) {
        return JWT.create()
                .withSubject("metacoding")
                .withClaim("id", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("imgUrl", user.getImgUrl())
                .withExpiresAt(Instant.now().plusMillis(EXPIRATION_TIME))
                .sign(Algorithm.HMAC512("metacoding"));
    }

    public static String createdRefreshToken(User user) {
        return JWT.create()
                .withSubject("metacoding-refresh")
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", user.getId())
                .withExpiresAt(Instant.now().plusMillis(EXPIRATION_REFRESH_TIME))
                .sign(Algorithm.HMAC512("metacoding-refresh-secret"));
    }

    public static User verify(String jwt)
            throws SignatureVerificationException, TokenExpiredException, JWTDecodeException {
        jwt = jwt.replace("Bearer ", "");
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512("metacoding"))
                .build().verify(jwt);
        int id = decodedJWT.getClaim("id").asInt();
        String username = decodedJWT.getClaim("username").asString();
        String imgUrl = decodedJWT.getClaim("imgUrl").asString();
        return User.builder().id(id).username(username).imgUrl(imgUrl).build();
    }

    public static int verifyRefreshToken(String refreshToken)
            throws SignatureVerificationException, TokenExpiredException, JWTDecodeException {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512("metacoding-refresh-secret"))
                .build().verify(refreshToken);
        return decodedJWT.getClaim("id").asInt();
    }
}
