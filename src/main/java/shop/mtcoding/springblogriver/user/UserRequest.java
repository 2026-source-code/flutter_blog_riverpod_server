package shop.mtcoding.springblogriver.user;

import lombok.Data;

public class UserRequest {

    @Data
    public static class JoinDTO {
        private String username;
        private String password;
        private String email;
        private String imgBase64;

        User toEntity(String encPassword, String imgUrl) {
            return User.builder()
                    .username(username)
                    .password(encPassword)
                    .email(email)
                    .imgUrl(imgUrl)
                    .build();
        }
    }

    @Data
    public static class LoginDTO {
        private String username;
        private String password;
        private String deviceId;
    }

    @Data
    public static class ReissueDTO {
        private String refreshToken;
        private String deviceId;
    }

    @Data
    public static class PasswordUpdateDTO {
        private String password;
    }

    @Data
    public static class ImgBase64UpdateDTO {
        private String imgBase64;
    }
}
