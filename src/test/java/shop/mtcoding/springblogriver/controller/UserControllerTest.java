package shop.mtcoding.springblogriver.controller;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import shop.mtcoding.springblogriver._core.auth.JwtUtil;
import shop.mtcoding.springblogriver._core.util.MyWithRestDoc;
import shop.mtcoding.springblogriver.user.User;
import shop.mtcoding.springblogriver.user.UserRequest;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Sql("classpath:db/teardown.sql")
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class UserControllerTest extends MyWithRestDoc {

    protected ObjectMapper om = new ObjectMapper();
    protected String accessToken;

    @BeforeEach
    public void setUp() {
        User user = User.builder()
                .id(1)
                .username("ssar")
                .password("1234")
                .email("ssar@nate.com")
                .imgUrl("/images/1.png")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        accessToken = JwtUtil.createdAccessToken(user);

        accessToken = "Bearer " + accessToken;
    }

    @Test
    public void join_test() throws Exception {
        String fakeBase64Image = "data:image/jpg;base64," +
                RandomStringUtils.randomAlphanumeric(1000);

        UserRequest.JoinDTO joinDTO = new UserRequest.JoinDTO();
        joinDTO.setUsername("hello");
        joinDTO.setPassword("1234");
        joinDTO.setEmail("hello@nate.com");
        joinDTO.setImgBase64(fakeBase64Image);

        String requestBody = om.writeValueAsString(joinDTO);
        System.out.println(requestBody);

        ResultActions resultActions = mvc.perform(
                post("/join").content(requestBody).contentType(MediaType.APPLICATION_JSON)
        );

        // console
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // verify
        resultActions.andExpect(jsonPath("$.success").value("true"));
        resultActions.andExpect(jsonPath("$.response.id").value(4));
        resultActions.andExpect(jsonPath("$.response.username").value("hello"));
        resultActions.andExpect(jsonPath("$.response.imgUrl").value(Matchers.endsWith(".jpg")));
        resultActions.andExpect(jsonPath("$.status").value(200));
        resultActions.andExpect(jsonPath("$.errorMessage").isEmpty());
        resultActions.andDo(MockMvcResultHandlers.print());
        resultActions.andDo(document);
    }

    @Test
    public void login_test() throws Exception {
        UserRequest.LoginDTO loginDTO = new UserRequest.LoginDTO();
        loginDTO.setUsername("ssar");
        loginDTO.setPassword("1234");

        String requestBody = om.writeValueAsString(loginDTO);
        System.out.println(requestBody);

        ResultActions resultActions = mvc.perform(
                post("/login").content(requestBody).contentType(MediaType.APPLICATION_JSON)
        );

        // console
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // 헤더에서 Authorization 값을 추출
        String authorizationHeaderValue = resultActions.andReturn().getResponse().getHeader("Authorization");
        System.out.println("Authorization 헤더 값: " + authorizationHeaderValue);

        // verify
        resultActions.andExpect(jsonPath("$.success").value("true"));
        resultActions.andExpect(jsonPath("$.response.id").value(1));
        resultActions.andExpect(jsonPath("$.response.username").value("ssar"));
        resultActions.andExpect(jsonPath("$.response.imgUrl").value("/images/1.png"));
        resultActions.andExpect(jsonPath("$.response.accessToken").exists());
        resultActions.andExpect(jsonPath("$.status").value(200));
        resultActions.andExpect(jsonPath("$.errorMessage").isEmpty());
        resultActions.andDo(MockMvcResultHandlers.print());
        resultActions.andDo(document);
    }

    //  /api/user/{id}/password
    // /api/user/{id}/img

    @Test
    public void user_password_update_test() throws Exception {
        Integer id = 1;
        UserRequest.PasswordUpdateDTO passwordUpdateDTO = new UserRequest.PasswordUpdateDTO();
        passwordUpdateDTO.setPassword("5678");

        String requestBody = om.writeValueAsString(passwordUpdateDTO);
        System.out.println(requestBody);

        ResultActions resultActions = mvc.perform(
                put("/api/user/$id/password".replace("$id", id.toString())).header("Authorization", accessToken).content(requestBody).contentType(MediaType.APPLICATION_JSON)
        );

        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        resultActions.andExpect(jsonPath("$.success").value("true"));
        resultActions.andExpect(jsonPath("$.response").isEmpty());
        resultActions.andExpect(jsonPath("$.status").value(200));
        resultActions.andExpect(jsonPath("$.errorMessage").isEmpty());
        resultActions.andDo(MockMvcResultHandlers.print());
        resultActions.andDo(document);
    }

    @Test
    public void user_img_update_test() throws Exception {
        Integer id = 1;

        String fakeBase64Image = "data:image/jpg;base64," +
                RandomStringUtils.randomAlphanumeric(1000);

        UserRequest.ImgBase64UpdateDTO imgBase64UpdateDTO = new UserRequest.ImgBase64UpdateDTO();
        imgBase64UpdateDTO.setImgBase64(fakeBase64Image);

        String requestBody = om.writeValueAsString(imgBase64UpdateDTO);
        System.out.println(requestBody);

        ResultActions resultActions = mvc.perform(
                put("/api/user/$id/img".replace("$id", id.toString())).header("Authorization", accessToken).content(requestBody).contentType(MediaType.APPLICATION_JSON)
        );

        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        resultActions.andExpect(jsonPath("$.success").value("true"));
        resultActions.andExpect(jsonPath("$.response.id").value(1));
        resultActions.andExpect(jsonPath("$.response.username").value("ssar"));
        resultActions.andExpect(jsonPath("$.response.imgUrl").value(Matchers.endsWith(".jpg")));
        resultActions.andExpect(jsonPath("$.status").value(200));
        resultActions.andExpect(jsonPath("$.errorMessage").isEmpty());
        resultActions.andDo(MockMvcResultHandlers.print());
        resultActions.andDo(document);
    }

    @Test
    public void userinfo_test() throws Exception {
        Integer id = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/user/$id".replace("$id", id.toString())).header("Authorization", accessToken)
        );

        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        resultActions.andExpect(jsonPath("$.success").value("true"));
        resultActions.andExpect(jsonPath("$.response.id").value(1));
        resultActions.andExpect(jsonPath("$.response.username").value("ssar"));
        resultActions.andExpect(jsonPath("$.response.imgUrl").value(Matchers.endsWith(".png")));
        resultActions.andExpect(jsonPath("$.status").value(200));
        resultActions.andExpect(jsonPath("$.errorMessage").isEmpty());
        resultActions.andDo(MockMvcResultHandlers.print());
        resultActions.andDo(document);
    }


    @Test
    public void auto_login_test() throws Exception {
        ResultActions resultActions = mvc.perform(
                post("/auto/login").header("Authorization", accessToken)
        );

        // console
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // verify
        resultActions.andExpect(jsonPath("$.success").value("true"));
        resultActions.andExpect(jsonPath("$.response.id").value(1));
        resultActions.andExpect(jsonPath("$.response.username").value("ssar"));
        resultActions.andExpect(jsonPath("$.response.imgUrl").value("/images/1.png"));
        resultActions.andExpect(jsonPath("$.status").value(200));
        resultActions.andExpect(jsonPath("$.errorMessage").isEmpty());
        resultActions.andDo(MockMvcResultHandlers.print());
        resultActions.andDo(document);
    }
}
