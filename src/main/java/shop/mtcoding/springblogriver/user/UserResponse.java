package shop.mtcoding.springblogriver.user;

import java.time.format.DateTimeFormatter;

public class UserResponse {

    public record DTO(Integer id, String username, String imgUrl) {
        public DTO(User user) {
            this(user.getId(), user.getUsername(), user.getImgUrl());
        }
    }

    public record DetailDTO(Integer id, String username, String email, String imgUrl, String createdAt,
                            String updatedAt) {
        public DetailDTO(User user) {
            this(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getImgUrl(),
                    user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                    user.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            );
        }
    }

    // jwt는 service -> controller 넘어갈 때만 사용
    record LoginDTO(String accessToken, Integer id, String username, String imgUrl) {
        LoginDTO(String accessToken, User user) {
            this(accessToken, user.getId(), user.getUsername(), user.getImgUrl());
        }
    }

    record AutoLoginDTO(Integer id, String username, String imgUrl) {
        AutoLoginDTO(User user) {
            this(user.getId(), user.getUsername(), user.getImgUrl());
        }
    }
}