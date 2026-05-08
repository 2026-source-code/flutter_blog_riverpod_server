package shop.mtcoding.springblogriver.user;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.mtcoding.springblogriver._core.auth.JwtEnum;
import shop.mtcoding.springblogriver._core.auth.JwtUtil;
import shop.mtcoding.springblogriver._core.auth.PasswordUtil;
import shop.mtcoding.springblogriver._core.error.exception.Exception400;
import shop.mtcoding.springblogriver._core.error.exception.Exception401;
import shop.mtcoding.springblogriver._core.error.exception.Exception404;
import shop.mtcoding.springblogriver._core.util.MyFileUtil;
import shop.mtcoding.springblogriver.token.Token;
import shop.mtcoding.springblogriver.token.TokenRepository;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Transactional
    public UserResponse.DTO 회원가입(UserRequest.JoinDTO requestDTO) {
        Optional<User> userOP = userRepository.findByUsername(requestDTO.getUsername());
        if (userOP.isPresent()) {
            throw new Exception400("유저네임 중복");
        }
        String encPassword = PasswordUtil.encode(requestDTO.getPassword());
        String imgUrl;
        try {
            imgUrl = MyFileUtil.write(requestDTO.getImgBase64());
        } catch (Exception e) {
            imgUrl = "/images/1.png";
        }
        User userPS = userRepository.save(requestDTO.toEntity(encPassword, imgUrl));
        return new UserResponse.DTO(userPS);
    }

    @Transactional
    public UserResponse.LoginDTO 로그인(UserRequest.LoginDTO requestDTO) {
        User userPS = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow(
                () -> new Exception401("유저네임을 찾을 수 없습니다")
        );
        if (!PasswordUtil.verify(requestDTO.getPassword(), userPS.getPassword())) {
            throw new Exception401("패스워드가 일치하지 않습니다");
        }
        String deviceId = requestDTO.getDeviceId();
        if (deviceId == null || deviceId.isBlank()) {
            throw new Exception400("deviceId가 필요합니다");
        }

        String accessToken = "Bearer " + JwtUtil.createdAccessToken(userPS);
        String refreshToken = JwtUtil.createdRefreshToken(userPS);

        Optional<Token> tokenOP = tokenRepository.findByUserId(userPS.getId());
        if (tokenOP.isPresent()) {
            tokenRepository.delete(tokenOP.get());
            tokenRepository.flush();
        }
        {
            tokenRepository.save(Token.builder()
                    .user(userPS)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .deviceId(deviceId)
                    .build());
        }

        System.out.println("accessToken : " + accessToken);
        System.out.println("refreshToken : " + refreshToken);

        return new UserResponse.LoginDTO(accessToken, refreshToken, userPS);
    }

    @Transactional
    public UserResponse.ReissueDTO 리이슈(UserRequest.ReissueDTO requestDTO) {
        String incomingRefreshToken = requestDTO.getRefreshToken();
        String incomingDeviceId = requestDTO.getDeviceId();

        int userId;
        try {
            userId = JwtUtil.verifyRefreshToken(incomingRefreshToken);
        } catch (SignatureVerificationException | JWTDecodeException e) {
            throw new Exception401(JwtEnum.REFRESH_TOKEN_INVALID.name());
        } catch (TokenExpiredException e) {
            throw new Exception401(JwtEnum.REFRESH_TOKEN_TIMEOUT.name());
        }

        Optional<Token> currentTokenOP = tokenRepository.findByRefreshToken(incomingRefreshToken);
        if (currentTokenOP.isPresent()) {
            Token tokenPS = currentTokenOP.get();

            if (!tokenPS.getDeviceId().equals(incomingDeviceId)) {
                throw new Exception401(JwtEnum.REFRESH_TOKEN_NOT_MATCH_SAVED_REDIS.name());
            }

            User userPS = userRepository.findById(userId).orElseThrow(
                    () -> new Exception401("유저를 찾을 수 없습니다")
            );

            String oldRefreshToken = tokenPS.getRefreshToken();
            String newAccessToken = "Bearer " + JwtUtil.createdAccessToken(userPS);
            String newRefreshToken = JwtUtil.createdRefreshToken(userPS);

            tokenRepository.delete(tokenPS);
            tokenRepository.flush();
            tokenRepository.save(Token.builder()
                    .user(userPS)
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .previousRefreshToken(oldRefreshToken)
                    .deviceId(incomingDeviceId)
                    .build());

            return new UserResponse.ReissueDTO(newAccessToken, newRefreshToken);
        }

        // Reuse Detection: 이미 폐기된 토큰 재사용 시 토큰 패밀리 전체 무효화
        Optional<Token> reuseTokenOP = tokenRepository.findByPreviousRefreshToken(incomingRefreshToken);
        if (reuseTokenOP.isPresent()) {
            tokenRepository.delete(reuseTokenOP.get());
            throw new Exception401(JwtEnum.REFRESH_TOKEN_REUSED.name());
        }

        throw new Exception401(JwtEnum.REFRESH_TOKEN_NOT_FOUND.name());
    }

    public UserResponse.AutoLoginDTO 자동로그인(String accessToken) {
        Optional.ofNullable(accessToken).orElseThrow(
                () -> new Exception401(JwtEnum.ACCESS_TOKEN_NOT_FOUND.name()));
        try {
            User user = JwtUtil.verify(accessToken);
            User userPS = userRepository.findByUsername(user.getUsername()).orElseThrow(
                    () -> new Exception401("유저네임을 찾을 수 없습니다")
            );
            return new UserResponse.AutoLoginDTO(userPS);
        } catch (SignatureVerificationException | JWTDecodeException e1) {
            throw new Exception401(JwtEnum.ACCESS_TOKEN_INVALID.name());
        } catch (TokenExpiredException e2) {
            throw new Exception401(JwtEnum.ACCESS_TOKEN_TIMEOUT.name());
        }
    }

    public List<UserResponse.DTO> 회원목록보기() {
        List<User> usersPS = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return usersPS.stream().map(UserResponse.DTO::new).toList();
    }

    public UserResponse.DetailDTO 회원정보보기(int id) {
        User userPS = userRepository.findById(id).orElseThrow(
                () -> new Exception404("id가 존재하지 않습니다 : " + id)
        );
        return new UserResponse.DetailDTO(userPS);
    }

    @Transactional
    public void 패스워드수정(int id, UserRequest.PasswordUpdateDTO requestDTO) {
        User userPS = userRepository.findById(id).orElseThrow(
                () -> new Exception404("id가 존재하지 않습니다 : " + id)
        );
        String encPassword = PasswordUtil.encode(requestDTO.getPassword());
        userPS.updatePassword(encPassword);
    }

    @Transactional
    public UserResponse.DTO 프로필사진수정(int id, UserRequest.ImgBase64UpdateDTO requestDTO) {
        User userPS = userRepository.findById(id).orElseThrow(
                () -> new Exception404("id가 존재하지 않습니다 : " + id)
        );
        String imgUrl = MyFileUtil.write(requestDTO.getImgBase64());
        userPS.updateImgUrl(imgUrl);
        return new UserResponse.DTO(userPS);
    }
}
